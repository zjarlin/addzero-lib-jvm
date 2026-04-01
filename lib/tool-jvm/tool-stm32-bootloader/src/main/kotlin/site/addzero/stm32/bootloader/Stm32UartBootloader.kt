package site.addzero.stm32.bootloader

import java.io.ByteArrayOutputStream
import java.io.Closeable
import kotlin.experimental.xor

/**
 * 基于 STM32 ROM USART Bootloader 的纯 Kotlin/JVM 烧录客户端。
 *
 * 这个类只实现官方公开的串口协议，
 * 不依赖 STM32CubeProgrammer、OpenOCD、JLink 等外部 CLI。
 */
class Stm32UartBootloader private constructor(
    val config: Stm32BootloaderConfig,
    private val transport: Stm32BootloaderTransport,
    private val sleeper: Stm32BootloaderSleeper,
) : Closeable {
    private var cachedBootloaderInfo: Stm32BootloaderInfo? = null

    constructor(config: Stm32BootloaderConfig) : this(
        config = config,
        transport = SerialBootloaderTransportFactory.open(config),
        sleeper = ThreadBootloaderSleeper,
    )

    internal constructor(
        config: Stm32BootloaderConfig,
        transport: Stm32BootloaderTransport,
        sleeper: Stm32BootloaderSleeper,
        marker: Boolean = true,
    ) : this(config, transport, sleeper)

    /**
     * 执行 BOOT0/NRST 时序，让芯片下次启动进入 System Memory Bootloader。
     *
     * 如果你的板子没有把 BOOT0 / NRST 接到串口控制线，
     * 就不要调用这个方法，改为人工按键或用外部控制器进入 Bootloader。
     */
    @Synchronized
    fun enterBootloaderMode() {
        val lineControl = requireNotNull(config.lineControl) {
            "未配置 lineControl，无法自动进入系统 Bootloader"
        }
        lineControl.boot0?.let { boot0 ->
            setSignal(boot0, asserted = true)
            sleeper.sleep(lineControl.boot0SetupDelayMs)
        }
        pulseReset(lineControl)
        sleeper.sleep(lineControl.bootloaderReadyDelayMs)
        transport.clearBuffers()
        cachedBootloaderInfo = null
    }

    /**
     * 仅做一次硬件复位。
     */
    @Synchronized
    fun resetTarget() {
        val lineControl = requireNotNull(config.lineControl) {
            "未配置 lineControl，无法自动复位目标设备"
        }
        pulseReset(lineControl)
        cachedBootloaderInfo = null
    }

    /**
     * 把 BOOT0 拉回用户 Flash，然后通过复位重新启动。
     *
     * 这比单纯发 `Go` 更适合你确实把 BOOT0 接出来控制的场景，
     * 因为它能保证后续再次硬复位时仍会从用户 Flash 启动。
     */
    @Synchronized
    fun bootFromFlash() {
        val lineControl = requireNotNull(config.lineControl) {
            "未配置 lineControl，无法自动切回用户 Flash"
        }
        lineControl.boot0?.let { boot0 ->
            setSignal(boot0, asserted = false)
        }
        pulseReset(lineControl)
        sleeper.sleep(lineControl.flashBootReadyDelayMs)
        cachedBootloaderInfo = null
    }

    /**
     * 同步并读取设备 Bootloader 能力。
     */
    @Synchronized
    fun connect(): Stm32BootloaderInfo {
        cachedBootloaderInfo?.let { info ->
            return info
        }

        var lastError: Throwable? = null
        repeat(config.connectAttempts) { attempt ->
            try {
                if (attempt == 0 && config.autoEnterBootloaderOnConnect) {
                    enterBootloaderMode()
                }
                if (attempt > 0) {
                    if (config.autoEnterBootloaderOnConnect && config.lineControl != null) {
                        enterBootloaderMode()
                    } else {
                        transport.clearBuffers()
                    }
                    sleeper.sleep(config.connectRetryDelayMs)
                }

                sync()
                val info = readBootloaderInfo()
                cachedBootloaderInfo = info
                return info
            } catch (throwable: Throwable) {
                lastError = throwable
            }
        }

        throw Stm32BootloaderException(
            "连接 STM32 USART Bootloader 失败，已尝试 ${config.connectAttempts} 次",
            lastError,
        )
    }

    /**
     * 读取任意有效内存区域。
     */
    @Synchronized
    fun readMemory(
        startAddress: Long,
        size: Int,
    ): ByteArray {
        require(startAddress in 0..MAX_U32_VALUE) {
            "startAddress 必须在 0..0xFFFFFFFF"
        }
        require(size > 0) {
            "size 必须大于 0"
        }
        ensureConnectedCommand(Stm32BootloaderCommand.READ_MEMORY)

        val out = ByteArrayOutputStream(size)
        var address = startAddress
        var remaining = size
        while (remaining > 0) {
            val chunkSize = remaining.coerceAtMost(config.readChunkSize)
            sendCommand(Stm32BootloaderCommand.READ_MEMORY)
            sendAddress(address)
            sendReadLength(chunkSize)
            out.write(transport.readExact(chunkSize))
            address += chunkSize.toLong()
            remaining -= chunkSize
        }
        return out.toByteArray()
    }

    /**
     * 写入任意有效内存区域。
     *
     * 尾块不足 4 字节时会按 [padByte] 自动补齐。
     */
    @Synchronized
    fun writeMemory(
        startAddress: Long,
        data: ByteArray,
        padByte: Byte = 0xFF.toByte(),
    ) {
        require(startAddress in 0..MAX_U32_VALUE) {
            "startAddress 必须在 0..0xFFFFFFFF"
        }
        require(data.isNotEmpty()) {
            "data 不能为空"
        }
        ensureConnectedCommand(Stm32BootloaderCommand.WRITE_MEMORY)

        var offset = 0
        var address = startAddress
        while (offset < data.size) {
            val remaining = data.size - offset
            val rawChunkSize = remaining.coerceAtMost(config.writeChunkSize)
            val actualChunk = data.copyOfRange(offset, offset + rawChunkSize)
            val paddedChunk = actualChunk.padTo4Bytes(padByte)

            sendCommand(Stm32BootloaderCommand.WRITE_MEMORY)
            sendAddress(address)
            sendWritePayload(paddedChunk)

            offset += rawChunkSize
            address += paddedChunk.size.toLong()
        }
    }

    /**
     * 按官方返回命令自动选择擦除指令。
     */
    @Synchronized
    fun erase(eraseMode: Stm32EraseMode): Stm32BootloaderCommand? {
        if (eraseMode == Stm32EraseMode.None) {
            return null
        }
        val info = connect()
        val command =
            when {
                info.supportedCommands.contains(Stm32BootloaderCommand.EXTENDED_ERASE_MEMORY) -> {
                    Stm32BootloaderCommand.EXTENDED_ERASE_MEMORY
                }

                info.supportedCommands.contains(Stm32BootloaderCommand.ERASE_MEMORY) -> {
                    Stm32BootloaderCommand.ERASE_MEMORY
                }

                else -> {
                    throw Stm32BootloaderException("设备未声明支持 Erase / Extended Erase 命令")
                }
            }

        when (command) {
            Stm32BootloaderCommand.EXTENDED_ERASE_MEMORY -> eraseWithExtendedCommand(eraseMode)
            Stm32BootloaderCommand.ERASE_MEMORY -> eraseWithLegacyCommand(eraseMode)
            else -> error("不可能走到这里")
        }
        return command
    }

    /**
     * 回读校验。
     */
    @Synchronized
    fun verifyMemory(
        startAddress: Long,
        expected: ByteArray,
    ) {
        val actual = readMemory(startAddress, expected.size)
        if (!actual.contentEquals(expected)) {
            throw Stm32BootloaderException(
                "回读校验失败：address=${startAddress.toHexAddress()} size=${expected.size}",
            )
        }
    }

    /**
     * 跳转到指定地址执行。
     *
     * 对普通 Flash 启动场景，应传应用基地址而不是 ResetHandler 地址。
     */
    @Synchronized
    fun go(startAddress: Long = DEFAULT_FLASH_BASE_ADDRESS) {
        require(startAddress in 0..MAX_U32_VALUE) {
            "startAddress 必须在 0..0xFFFFFFFF"
        }
        ensureConnectedCommand(Stm32BootloaderCommand.GO)
        sendCommand(Stm32BootloaderCommand.GO)
        sendAddress(startAddress)
    }

    /**
     * 一次完整烧录流程。
     */
    @Synchronized
    fun program(request: Stm32FlashRequest): Stm32FlashReport {
        return program(request, NoopStm32FlashProgressListener)
    }

    /**
     * 一次完整烧录流程，并持续汇报阶段进度。
     */
    @Synchronized
    fun program(
        request: Stm32FlashRequest,
        progressListener: Stm32FlashProgressListener,
    ): Stm32FlashReport {
        val progressTracker = Stm32FlashProgressTracker(request, progressListener)
        if (config.autoEnterBootloaderOnConnect && config.lineControl != null) {
            progressTracker.report(
                stage = Stm32FlashStage.ENTER_BOOTLOADER,
                stageCompletedBytes = 0,
                stageTotalBytes = 0,
                overallPercent = 0.0,
                stagePercent = 0.0,
                message = "准备进入系统 Bootloader",
            )
        }
        progressTracker.report(
            stage = Stm32FlashStage.CONNECTING,
            stageCompletedBytes = 0,
            stageTotalBytes = 0,
            overallPercent = progressTracker.connectingWeight.startPercent,
            stagePercent = 0.0,
            message = "正在连接 Bootloader",
        )
        val info = connect()
        progressTracker.reportStageDone(
            stage = Stm32FlashStage.CONNECTING,
            overallPercent = progressTracker.connectingWeight.endPercent,
            message = "Bootloader 连接成功，芯片 ID=0x${info.chipId.toString(16).uppercase()}",
        )

        val eraseCommand =
            if (request.eraseMode == Stm32EraseMode.None) {
                null
            } else {
                progressTracker.report(
                    stage = Stm32FlashStage.ERASING,
                    stageCompletedBytes = 0,
                    stageTotalBytes = 0,
                    overallPercent = progressTracker.erasingWeight.startPercent,
                    stagePercent = 0.0,
                    message = "正在擦除 Flash",
                )
                erase(request.eraseMode).also { command ->
                    progressTracker.reportStageDone(
                        stage = Stm32FlashStage.ERASING,
                        overallPercent = progressTracker.erasingWeight.endPercent,
                        message = "Flash 擦除完成${command?.let { "，命令=${it.name}" } ?: ""}",
                    )
                }
            }

        progressTracker.report(
            stage = Stm32FlashStage.WRITING,
            stageCompletedBytes = 0,
            stageTotalBytes = request.firmware.size.toLong(),
            overallPercent = progressTracker.writingWeight.startPercent,
            stagePercent = 0.0,
            message = "正在写入固件",
        )
        writeMemoryWithProgress(
            startAddress = request.startAddress,
            data = request.firmware,
            padByte = request.padByte,
        ) { writtenBytes ->
            progressTracker.reportByteStage(
                stage = Stm32FlashStage.WRITING,
                completedBytes = writtenBytes.toLong(),
                totalBytes = request.firmware.size.toLong(),
                weight = progressTracker.writingWeight,
                message = "正在写入固件 $writtenBytes/${request.firmware.size} bytes",
            )
        }

        if (request.verifyAfterWrite) {
            progressTracker.report(
                stage = Stm32FlashStage.VERIFYING,
                stageCompletedBytes = 0,
                stageTotalBytes = request.firmware.size.toLong(),
                overallPercent = progressTracker.verifyingWeight.startPercent,
                stagePercent = 0.0,
                message = "正在回读校验",
            )
            verifyMemoryWithProgress(
                startAddress = request.startAddress,
                expected = request.firmware,
            ) { verifiedBytes ->
                progressTracker.reportByteStage(
                    stage = Stm32FlashStage.VERIFYING,
                    completedBytes = verifiedBytes.toLong(),
                    totalBytes = request.firmware.size.toLong(),
                    weight = progressTracker.verifyingWeight,
                    message = "正在回读校验 $verifiedBytes/${request.firmware.size} bytes",
                )
            }
        } else {
            progressTracker.skipVerify()
        }

        val started =
            if (request.startApplicationAfterWrite) {
                progressTracker.report(
                    stage = Stm32FlashStage.STARTING_APPLICATION,
                    stageCompletedBytes = 0,
                    stageTotalBytes = 0,
                    overallPercent = progressTracker.startingWeight.startPercent,
                    stagePercent = 0.0,
                    message = "正在启动应用",
                )
                if (config.lineControl?.reset != null) {
                    bootFromFlash()
                } else {
                    if (config.lineControl?.boot0 != null) {
                        setSignal(config.lineControl.boot0, asserted = false)
                    }
                    go(request.startAddress)
                }
                progressTracker.reportStageDone(
                    stage = Stm32FlashStage.STARTING_APPLICATION,
                    overallPercent = progressTracker.startingWeight.endPercent,
                    message = "应用已启动",
                )
                true
            } else {
                progressTracker.skipStart()
                false
            }

        progressTracker.report(
            stage = Stm32FlashStage.COMPLETED,
            stageCompletedBytes = request.firmware.size.toLong(),
            stageTotalBytes = request.firmware.size.toLong(),
            overallPercent = 100.0,
            stagePercent = 100.0,
            message = "烧录完成",
        )

        return Stm32FlashReport(
            bootloaderInfo = info,
            bytesWritten = request.firmware.size,
            eraseCommand = eraseCommand,
            verified = request.verifyAfterWrite,
            startedApplication = started,
        )
    }

    override fun close() {
        transport.close()
    }

    private fun eraseWithLegacyCommand(eraseMode: Stm32EraseMode) {
        sendCommand(Stm32BootloaderCommand.ERASE_MEMORY)
        when (eraseMode) {
            Stm32EraseMode.None -> {
                return
            }

            Stm32EraseMode.Mass -> {
                transport.write(byteArrayOf(0xFF.toByte(), 0x00))
            }

            is Stm32EraseMode.PageCodes -> {
                require(eraseMode.codes.all { code -> code in 0..0xFF }) {
                    "Legacy Erase 只支持 0..255 的单字节页码"
                }
                val count = eraseMode.codes.size - 1
                val payload = ByteArray(eraseMode.codes.size + 2)
                payload[0] = count.toByte()
                eraseMode.codes.forEachIndexed { index, code ->
                    payload[index + 1] = code.toByte()
                }
                payload[payload.lastIndex] = xorChecksum(payload, 0, payload.size - 1)
                transport.write(payload)
            }
        }
        expectAck("Erase Memory 被设备拒绝")
    }

    private fun eraseWithExtendedCommand(eraseMode: Stm32EraseMode) {
        sendCommand(Stm32BootloaderCommand.EXTENDED_ERASE_MEMORY)
        when (eraseMode) {
            Stm32EraseMode.None -> {
                return
            }

            Stm32EraseMode.Mass -> {
                transport.write(byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0x00))
            }

            is Stm32EraseMode.PageCodes -> {
                val count = eraseMode.codes.size - 1
                val payloadSize = 2 + (eraseMode.codes.size * 2) + 1
                val payload = ByteArray(payloadSize)
                payload[0] = ((count ushr 8) and 0xFF).toByte()
                payload[1] = (count and 0xFF).toByte()
                eraseMode.codes.forEachIndexed { index, code ->
                    val offset = 2 + (index * 2)
                    payload[offset] = ((code ushr 8) and 0xFF).toByte()
                    payload[offset + 1] = (code and 0xFF).toByte()
                }
                payload[payload.lastIndex] = xorChecksum(payload, 0, payload.size - 1)
                transport.write(payload)
            }
        }
        expectAck("Extended Erase Memory 被设备拒绝")
    }

    private fun readBootloaderInfo(): Stm32BootloaderInfo {
        val getPayload = transactGet()
        val versionPayload = transactGetVersion()
        val chipId = transactGetId()
        val rawCommandCodes = getPayload.commandCodes.toSet()
        return Stm32BootloaderInfo(
            protocolVersion = getPayload.protocolVersion,
            bootloaderVersion = versionPayload.version,
            chipId = chipId,
            optionByte1 = versionPayload.optionByte1,
            optionByte2 = versionPayload.optionByte2,
            supportedCommandCodes = rawCommandCodes,
            supportedCommands = rawCommandCodes.mapNotNull(Stm32BootloaderCommand::fromOpcode).toSet(),
        )
    }

    private fun transactGet(): GetPayload {
        sendCommand(Stm32BootloaderCommand.GET)
        val count = readUnsignedByte()
        val payload = transport.readExact(count + 1)
        expectAck("Get 命令尾 ACK 丢失")
        return GetPayload(
            protocolVersion = payload.first().toUnsignedInt(),
            commandCodes = payload.drop(1).map { byte -> byte.toUnsignedInt() },
        )
    }

    private fun transactGetVersion(): VersionPayload {
        sendCommand(Stm32BootloaderCommand.GET_VERSION)
        val payload = transport.readExact(3)
        expectAck("Get Version 命令尾 ACK 丢失")
        return VersionPayload(
            version = payload[0].toUnsignedInt(),
            optionByte1 = payload[1].toUnsignedInt(),
            optionByte2 = payload[2].toUnsignedInt(),
        )
    }

    private fun transactGetId(): Int {
        sendCommand(Stm32BootloaderCommand.GET_ID)
        val count = readUnsignedByte()
        val payload = transport.readExact(count + 1)
        expectAck("Get ID 命令尾 ACK 丢失")
        return payload.fold(0) { acc, byte ->
            (acc shl 8) or byte.toUnsignedInt()
        }
    }

    private fun sync() {
        transport.clearBuffers()
        transport.write(byteArrayOf(SYNC))
        expectAck("同步 0x7F 未收到 ACK")
    }

    private fun ensureConnectedCommand(command: Stm32BootloaderCommand) {
        val info = connect()
        if (command !in info.supportedCommands) {
            throw Stm32BootloaderException(
                "设备不支持命令 ${command.name}(0x${command.opcode.toString(16).uppercase()})",
            )
        }
    }

    private fun sendCommand(command: Stm32BootloaderCommand) {
        val opcode = command.opcode.toByte()
        transport.write(byteArrayOf(opcode, opcode xor 0xFF.toByte()))
        expectAck("命令 ${command.name} 被设备拒绝")
    }

    private fun sendAddress(address: Long) {
        val frame = addressToFrame(address)
        transport.write(frame)
        expectAck("地址 ${address.toHexAddress()} 被设备拒绝")
    }

    private fun sendReadLength(size: Int) {
        require(size in 1..Stm32BootloaderConfig.MAX_CHUNK_SIZE) {
            "读取长度必须在 1..${Stm32BootloaderConfig.MAX_CHUNK_SIZE}"
        }
        val lengthByte = (size - 1).toByte()
        transport.write(byteArrayOf(lengthByte, lengthByte xor 0xFF.toByte()))
        expectAck("读取长度 $size 被设备拒绝")
    }

    private fun sendWritePayload(chunk: ByteArray) {
        require(chunk.isNotEmpty()) {
            "chunk 不能为空"
        }
        require(chunk.size <= Stm32BootloaderConfig.MAX_CHUNK_SIZE) {
            "chunk 最大只能 ${Stm32BootloaderConfig.MAX_CHUNK_SIZE} 字节"
        }
        require(chunk.size % 4 == 0) {
            "chunk 大小必须是 4 的倍数"
        }

        val payload = ByteArray(chunk.size + 2)
        payload[0] = (chunk.size - 1).toByte()
        chunk.copyInto(payload, destinationOffset = 1)
        payload[payload.lastIndex] = xorChecksum(payload, 0, payload.size - 1)
        transport.write(payload)
        expectAck("写入 ${chunk.size} 字节被设备拒绝")
    }

    private fun writeMemoryWithProgress(
        startAddress: Long,
        data: ByteArray,
        padByte: Byte,
        onChunkWritten: (writtenBytes: Int) -> Unit,
    ) {
        require(startAddress in 0..MAX_U32_VALUE) {
            "startAddress 必须在 0..0xFFFFFFFF"
        }
        require(data.isNotEmpty()) {
            "data 不能为空"
        }
        ensureConnectedCommand(Stm32BootloaderCommand.WRITE_MEMORY)

        var offset = 0
        var address = startAddress
        while (offset < data.size) {
            val remaining = data.size - offset
            val rawChunkSize = remaining.coerceAtMost(config.writeChunkSize)
            val actualChunk = data.copyOfRange(offset, offset + rawChunkSize)
            val paddedChunk = actualChunk.padTo4Bytes(padByte)

            sendCommand(Stm32BootloaderCommand.WRITE_MEMORY)
            sendAddress(address)
            sendWritePayload(paddedChunk)

            offset += rawChunkSize
            address += paddedChunk.size.toLong()
            onChunkWritten(offset)
        }
    }

    private fun verifyMemoryWithProgress(
        startAddress: Long,
        expected: ByteArray,
        onChunkVerified: (verifiedBytes: Int) -> Unit,
    ) {
        require(startAddress in 0..MAX_U32_VALUE) {
            "startAddress 必须在 0..0xFFFFFFFF"
        }
        require(expected.isNotEmpty()) {
            "expected 不能为空"
        }
        ensureConnectedCommand(Stm32BootloaderCommand.READ_MEMORY)

        var address = startAddress
        var offset = 0
        while (offset < expected.size) {
            val chunkSize = (expected.size - offset).coerceAtMost(config.readChunkSize)
            val actualChunk = readMemory(address, chunkSize)
            val expectedChunk = expected.copyOfRange(offset, offset + chunkSize)
            if (!actualChunk.contentEquals(expectedChunk)) {
                throw Stm32BootloaderException(
                    "回读校验失败：address=${address.toHexAddress()} size=$chunkSize",
                )
            }
            offset += chunkSize
            address += chunkSize.toLong()
            onChunkVerified(offset)
        }
    }

    private fun expectAck(message: String) {
        when (val response = readUnsignedByte()) {
            ACK.toUnsignedInt() -> {
                return
            }

            NACK.toUnsignedInt() -> {
                throw Stm32BootloaderNackException(message)
            }

            else -> {
                throw Stm32BootloaderProtocolException(
                    "$message，收到非法响应 0x${response.toString(16).uppercase()}",
                )
            }
        }
    }

    private fun readUnsignedByte(): Int {
        return transport.readExact(1)[0].toUnsignedInt()
    }

    private fun pulseReset(lineControl: Stm32BootloaderLineControl) {
        val reset = requireNotNull(lineControl.reset) {
            "lineControl.reset 未配置，无法自动复位目标设备"
        }
        setSignal(reset, asserted = true)
        sleeper.sleep(lineControl.resetPulseMs)
        setSignal(reset, asserted = false)
    }

    private fun setSignal(
        signal: Stm32ControlSignal,
        asserted: Boolean,
    ) {
        val output =
            if (asserted) {
                signal.assertedOutput
            } else {
                !signal.assertedOutput
            }
        transport.setLine(signal.line, output)
    }

    companion object {
        private val ACK = 0x79.toByte()
        private val NACK = 0x1F.toByte()
        private val SYNC = 0x7F.toByte()
    }
}

private class Stm32FlashProgressTracker(
    request: Stm32FlashRequest,
    private val progressListener: Stm32FlashProgressListener,
) {
    /**
     * 阶段权重是给 UI 进度条用的经验值，不是协议层真实耗时。
     *
     * 擦除时长受芯片和容量影响很大，因此这里只给一个较小固定权重，
     * 让进度条既不会长时间停在 0，也不会假装能精确预测擦除耗时。
     */
    val connectingWeight = ProgressWeight(0.0, 5.0)
    val erasingWeight = ProgressWeight(5.0, if (request.eraseMode == Stm32EraseMode.None) 5.0 else 15.0)
    val writingWeight = ProgressWeight(erasingWeight.endPercent, 80.0)
    val verifyingWeight = ProgressWeight(
        writingWeight.endPercent,
        if (request.verifyAfterWrite) 95.0 else writingWeight.endPercent,
    )
    val startingWeight = ProgressWeight(
        verifyingWeight.endPercent,
        if (request.startApplicationAfterWrite) 100.0 else verifyingWeight.endPercent,
    )

    fun report(
        stage: Stm32FlashStage,
        stageCompletedBytes: Long,
        stageTotalBytes: Long,
        overallPercent: Double,
        stagePercent: Double,
        message: String,
    ) {
        progressListener.onProgress(
            Stm32FlashProgress(
                stage = stage,
                stageCompletedBytes = stageCompletedBytes,
                stageTotalBytes = stageTotalBytes,
                overallPercent = overallPercent.coerceIn(0.0, 100.0),
                stagePercent = stagePercent.coerceIn(0.0, 100.0),
                message = message,
            ),
        )
    }

    fun reportStageDone(
        stage: Stm32FlashStage,
        overallPercent: Double,
        message: String,
    ) {
        report(
            stage = stage,
            stageCompletedBytes = 0,
            stageTotalBytes = 0,
            overallPercent = overallPercent,
            stagePercent = 100.0,
            message = message,
        )
    }

    fun reportByteStage(
        stage: Stm32FlashStage,
        completedBytes: Long,
        totalBytes: Long,
        weight: ProgressWeight,
        message: String,
    ) {
        val stagePercent =
            if (totalBytes <= 0) {
                100.0
            } else {
                completedBytes.toDouble() / totalBytes.toDouble() * 100.0
            }
        val overallPercent = weight.interpolate(stagePercent)
        report(
            stage = stage,
            stageCompletedBytes = completedBytes,
            stageTotalBytes = totalBytes,
            overallPercent = overallPercent,
            stagePercent = stagePercent,
            message = message,
        )
    }

    fun skipVerify() {
        reportStageDone(
            stage = Stm32FlashStage.VERIFYING,
            overallPercent = verifyingWeight.endPercent,
            message = "已跳过回读校验",
        )
    }

    fun skipStart() {
        reportStageDone(
            stage = Stm32FlashStage.STARTING_APPLICATION,
            overallPercent = startingWeight.endPercent,
            message = "已跳过启动应用",
        )
    }
}

private data class ProgressWeight(
    val startPercent: Double,
    val endPercent: Double,
) {
    fun interpolate(stagePercent: Double): Double {
        val normalized = (stagePercent.coerceIn(0.0, 100.0) / 100.0)
        return startPercent + (endPercent - startPercent) * normalized
    }
}

private data class GetPayload(
    val protocolVersion: Int,
    val commandCodes: List<Int>,
)

private data class VersionPayload(
    val version: Int,
    val optionByte1: Int,
    val optionByte2: Int,
)

private fun addressToFrame(address: Long): ByteArray {
    require(address in 0..MAX_U32_VALUE) {
        "address 必须在 0..0xFFFFFFFF"
    }
    val bytes =
        byteArrayOf(
            ((address ushr 24) and 0xFF).toByte(),
            ((address ushr 16) and 0xFF).toByte(),
            ((address ushr 8) and 0xFF).toByte(),
            (address and 0xFF).toByte(),
        )
    return bytes + xorChecksum(bytes, 0, bytes.size)
}

private fun ByteArray.padTo4Bytes(padByte: Byte): ByteArray {
    if (size % 4 == 0) {
        return this
    }
    val paddedSize = ((size + 3) / 4) * 4
    return ByteArray(paddedSize) { index ->
        if (index < size) {
            this[index]
        } else {
            padByte
        }
    }
}

private fun xorChecksum(
    bytes: ByteArray,
    startInclusive: Int,
    endExclusive: Int,
): Byte {
    var checksum = 0x00.toByte()
    for (index in startInclusive until endExclusive) {
        checksum = checksum xor bytes[index]
    }
    return checksum
}

private fun Byte.toUnsignedInt(): Int = toInt() and 0xFF

private fun Long.toHexAddress(): String = "0x${toString(16).uppercase().padStart(8, '0')}"

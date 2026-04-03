package site.addzero.stm32.bootloader

import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import java.io.Closeable

/**
 * 基于 ST-Link + SWD 的 STM32 连接入口。
 *
 * 当前先提供：
 * - ST-Link 发现与打开
 * - 进入 SWD
 * - 读取版本/电压/core id/chip id
 * - `NRST` 控制与系统复位
 * - Debug/Memory 级别的 32-bit 读写
 * - 已接入当前实测板子 `chipId=0x414` 的 Flash 擦写与进度回调
 */
class Stm32StLinkProgrammer(
    private val config: StLinkConfig = StLinkConfig(),
) : Closeable {
    private val session = StLinkUsbSession.open(config)
    private var cachedTargetInfo: Stm32SwdTargetInfo? = null
    private var cachedFlashGeometry: Stm32StLinkFlashGeometry? = null

    val probeInfo: StLinkProbeInfo
        get() = session.probeInfo

    fun readVersion(): StLinkVersion = session.readVersion()

    fun readTargetVoltageMillivolts(): Int = session.readTargetVoltageMillivolts()

    fun currentMode(): StLinkCurrentMode = session.currentMode()

    /**
     * 进入 SWD 调试模式。
     */
    @Synchronized
    fun enterSwd() {
        session.enterSwd()
    }

    /**
     * 读取 32-bit Debug 寄存器。
     */
    @Synchronized
    fun readDebug32(address: Long): Long {
        require(address in 0..MAX_U32_VALUE) {
            "address 必须在 0..0xFFFFFFFF"
        }
        return session.readDebug32(address)
    }

    /**
     * 写入 32-bit Debug 寄存器。
     */
    @Synchronized
    fun writeDebug32(
        address: Long,
        value: Long,
    ) {
        require(address in 0..MAX_U32_VALUE) {
            "address 必须在 0..0xFFFFFFFF"
        }
        require(value in 0..MAX_U32_VALUE) {
            "value 必须在 0..0xFFFFFFFF"
        }
        session.writeDebug32(address, value)
    }

    /**
     * 读取 32-bit Memory Access Port 可见内存。
     */
    @Synchronized
    fun readMemory32(
        address: Long,
        size: Int,
    ): ByteArray {
        require(address in 0..MAX_U32_VALUE) {
            "address 必须在 0..0xFFFFFFFF"
        }
        require(size > 0) {
            "size 必须大于 0"
        }
        require(size % 4 == 0) {
            "readMemory32 的 size 必须是 4 的倍数"
        }
        return session.readMemory32(address, size)
    }

    /**
     * 通过 ST-Link 的 32-bit memory write 写入目标内存。
     */
    @Synchronized
    fun writeMemory32(
        address: Long,
        data: ByteArray,
    ) {
        require(address in 0..MAX_U32_VALUE) {
            "address 必须在 0..0xFFFFFFFF"
        }
        require(data.isNotEmpty()) {
            "data 不能为空"
        }
        require(data.size % 4 == 0) {
            "writeMemory32 的 data.size 必须是 4 的倍数"
        }
        session.writeMemory32(address, data)
    }

    /**
     * 通过 ST-Link 的 8-bit memory write 写入目标内存。
     */
    @Synchronized
    fun writeMemory8(
        address: Long,
        data: ByteArray,
    ) {
        require(address in 0..MAX_U32_VALUE) {
            "address 必须在 0..0xFFFFFFFF"
        }
        require(data.isNotEmpty()) {
            "data 不能为空"
        }
        session.writeMemory8(address, data)
    }

    /**
     * 直接驱动 ST-Link 的 `NRST` 引脚。
     *
     * `asserted = true` 表示拉低复位线。
     */
    @Synchronized
    fun driveReset(asserted: Boolean) {
        session.driveReset(asserted)
    }

    /**
     * 发送一次 ST-Link 层系统复位命令。
     */
    @Synchronized
    fun resetSystem() {
        session.resetSystem()
    }

    /**
     * 执行一次硬件 `NRST` 脉冲。
     */
    @Synchronized
    fun pulseReset(
        lowTimeMs: Long = config.resetHoldTimeMs,
        highSettleTimeMs: Long = config.attachRetryDelayMs,
    ) {
        require(lowTimeMs >= 0) {
            "lowTimeMs 不能小于 0"
        }
        require(highSettleTimeMs >= 0) {
            "highSettleTimeMs 不能小于 0"
        }
        driveReset(asserted = true)
        sleep(lowTimeMs)
        driveReset(asserted = false)
        sleep(highSettleTimeMs)
    }

    /**
     * 连接目标芯片并读取核心识别信息。
     */
    @Synchronized
    fun connectTarget(): Stm32SwdTargetInfo {
        val version = session.readVersion()
        val targetVoltageMillivolts = session.readTargetVoltageMillivolts()
        attachTarget()
        val mode = session.currentMode()
        val coreId = session.readCoreId()
        val cpuId = session.readDebug32(STM32_REG_CM3_CPUID)
        val chipIdRegisterAddress = resolveChipIdRegisterAddress(coreId, cpuId)
        val chipId = (session.readDebug32(chipIdRegisterAddress) and 0xFFF).toInt()
        return Stm32SwdTargetInfo(
            probe = session.probeInfo,
            stLinkVersion = version,
            currentMode = mode,
            targetVoltageMillivolts = targetVoltageMillivolts,
            coreId = coreId,
            cpuId = cpuId,
            chipId = chipId,
            chipIdRegisterAddress = chipIdRegisterAddress,
        ).also { targetInfo ->
            cachedTargetInfo = targetInfo
        }
    }

    /**
     * 读取当前芯片的 Flash 几何信息。
     */
    @Synchronized
    fun readFlashGeometry(targetInfo: Stm32SwdTargetInfo? = null): Stm32StLinkFlashGeometry {
        val resolvedTargetInfo = targetInfo ?: cachedTargetInfo ?: connectTarget()
        val driver = resolveStm32StLinkFlashDriver(resolvedTargetInfo.chipId)
        return driver.readGeometry(this, resolvedTargetInfo).also { geometry ->
            cachedFlashGeometry = geometry
        }
    }

    /**
     * 通过 ST-Link + SWD 直接烧录目标 Flash。
     *
     * 当前优先覆盖本次实测芯片 `STM32F1xx HD (chipId=0x414)`。
     */
    @Synchronized
    fun flash(
        request: Stm32FlashRequest,
        progressListener: Stm32FlashProgressListener = NoopStm32FlashProgressListener,
    ): Stm32StLinkFlashReport {
        val targetInfo = cachedTargetInfo ?: connectTarget()
        val geometry = cachedFlashGeometry ?: readFlashGeometry(targetInfo)
        return flash(
            request = request,
            targetInfo = targetInfo,
            geometry = geometry,
            progressListener = progressListener,
        )
    }

    /**
     * 使用调用方已知的目标信息和几何信息执行烧录。
     *
     * 当业务已经完成一次探测，或者想把探测结果缓存下来复用时，
     * 用这个重载可以避免重复读 Flash 元信息。
     */
    @Synchronized
    fun flash(
        request: Stm32FlashRequest,
        targetInfo: Stm32SwdTargetInfo,
        geometry: Stm32StLinkFlashGeometry,
        progressListener: Stm32FlashProgressListener = NoopStm32FlashProgressListener,
    ): Stm32StLinkFlashReport {
        require(request.startAddress % 2L == 0L) {
            "当前 SWD Flash 写入要求 startAddress 按 2 字节对齐"
        }
        cachedTargetInfo = targetInfo
        cachedFlashGeometry = geometry
        val driver = resolveStm32StLinkFlashDriver(targetInfo.chipId)
        return driver.flash(
            programmer = this,
            request = request,
            targetInfo = targetInfo,
            geometry = geometry,
            progressListener = progressListener,
        )
    }

    /**
     * 软复位并保持 halt，便于后续安全做 Flash 操作。
     */
    @Synchronized
    fun softResetAndHalt() {
        session.softResetAndHalt()
    }

    internal fun releaseDebugHalt() {
        session.releaseDebugHalt()
    }

    override fun close() {
        session.close()
    }

    internal fun loadFlashGeometry(profile: Stm32StLinkFlashProfile): Stm32StLinkFlashGeometry {
        val flashSizeBytes = readFlashSizeBytes(profile)
        return Stm32StLinkFlashGeometry(
            flashBaseAddress = profile.flashBaseAddress,
            flashSizeBytes = flashSizeBytes,
            pageSizeBytes = profile.pageSizeBytes,
            sramBaseAddress = profile.sramBaseAddress,
            sramSizeBytes = profile.sramSizeBytes,
        )
    }

    internal fun readFlashSizeBytes(profile: Stm32StLinkFlashProfile): Int {
        val registerValue = session.readMemory32(profile.flashSizeRegisterAddress, 16).readLeUInt16(0).toLong()
        val sizeKilobytes = (registerValue and 0xFFFF).toInt()
        if (sizeKilobytes > 0) {
            return sizeKilobytes * 1024
        }
        profile.defaultFlashSizeBytes?.let { fallbackBytes ->
            return fallbackBytes
        }
        throw IllegalArgumentException("目标芯片返回了无效的 Flash 大小: 0x${registerValue.toString(16).uppercase()}")
    }

    internal fun validateFlashRange(
        geometry: Stm32StLinkFlashGeometry,
        startAddress: Long,
        length: Int,
    ) {
        require(startAddress >= geometry.flashBaseAddress) {
            "startAddress 不在目标 Flash 区间内"
        }
        val endExclusive = startAddress + length
        val flashEndExclusive = geometry.flashBaseAddress + geometry.flashSizeBytes
        require(endExclusive <= flashEndExclusive) {
            "写入范围超出目标 Flash 空间"
        }
    }

    internal fun eraseFlashMass(profile: Stm32StLinkFlashProfile) {
        session.unlockFlash(profile)
        session.waitFlashReady(profile)
        session.clearFlashStatus(profile)
        val control = session.readDebug32(profile.flashControlRegisterAddress)
        val preparedControl =
            control.clearBit(FLASH_CR_PG_BIT)
                .clearBit(FLASH_CR_PER_BIT)
                .setBit(FLASH_CR_MER_BIT)
        session.writeDebug32(profile.flashControlRegisterAddress, preparedControl)
        session.writeDebug32(
            profile.flashControlRegisterAddress,
            preparedControl.setBit(FLASH_CR_STRT_BIT),
        )
        session.waitFlashReady(profile)
        session.assertFlashOperationSucceeded(profile, "整片擦除")
        session.writeDebug32(
            profile.flashControlRegisterAddress,
            session.readDebug32(profile.flashControlRegisterAddress).clearBit(FLASH_CR_MER_BIT),
        )
        session.lockFlash(profile)
    }

    internal fun eraseFlashPage(
        profile: Stm32StLinkFlashProfile,
        pageAddress: Long,
    ) {
        require((pageAddress - profile.flashBaseAddress) % profile.pageSizeBytes.toLong() == 0L) {
            "pageAddress 必须按页大小对齐"
        }
        session.unlockFlash(profile)
        session.waitFlashReady(profile)
        session.clearFlashStatus(profile)
        val control =
            session.readDebug32(profile.flashControlRegisterAddress)
                .clearBit(FLASH_CR_PG_BIT)
                .setBit(FLASH_CR_PER_BIT)
        session.writeDebug32(profile.flashControlRegisterAddress, control)
        session.writeDebug32(profile.flashAddressRegisterAddress, pageAddress)
        session.writeDebug32(
            profile.flashControlRegisterAddress,
            control.setBit(FLASH_CR_STRT_BIT),
        )
        session.waitFlashReady(profile)
        session.assertFlashOperationSucceeded(profile, "页擦除")
        session.writeDebug32(
            profile.flashControlRegisterAddress,
            session.readDebug32(profile.flashControlRegisterAddress).clearBit(FLASH_CR_PER_BIT),
        )
        session.lockFlash(profile)
    }

    internal fun programFlashWithLoader(
        profile: Stm32StLinkFlashProfile,
        geometry: Stm32StLinkFlashGeometry,
        startAddress: Long,
        firmware: ByteArray,
        progressListener: Stm32FlashProgressListener,
    ) {
        session.unlockFlash(profile)
        session.waitFlashReady(profile)
        session.clearFlashStatus(profile)
        val control =
            session.readDebug32(profile.flashControlRegisterAddress)
                .clearBit(FLASH_CR_PER_BIT)
                .setBit(FLASH_CR_PG_BIT)
        session.writeDebug32(profile.flashControlRegisterAddress, control)
        val loader = session.prepareFlashLoader(profile, geometry)
        var written = 0
        while (written < firmware.size) {
            val chunkSize = minOf(profile.pageSizeBytes, firmware.size - written)
            val chunk = firmware.copyOfRange(written, written + chunkSize)
            session.runFlashLoaderChunk(
                loader = loader,
                profile = profile,
                targetAddress = startAddress + written,
                payload = chunk,
            )
            written += chunkSize
            reportProgress(
                progressListener = progressListener,
                stage = Stm32FlashStage.WRITING,
                overallPercent = 20.0 + (written.toDouble() / firmware.size.toDouble() * 60.0),
                stageCompletedBytes = written.toLong(),
                stageTotalBytes = firmware.size.toLong(),
                message = "已写入 $written / ${firmware.size} 字节",
            )
        }
        session.assertFlashOperationSucceeded(profile, "写入")
        session.writeDebug32(
            profile.flashControlRegisterAddress,
            session.readDebug32(profile.flashControlRegisterAddress).clearBit(FLASH_CR_PG_BIT),
        )
        session.lockFlash(profile)
    }

    internal fun verifyFlash(
        startAddress: Long,
        expected: ByteArray,
        progressListener: Stm32FlashProgressListener,
    ) {
        var verified = 0
        val chunkSize = 256
        while (verified < expected.size) {
            val size = minOf(chunkSize, expected.size - verified)
            val actual = readMemoryAligned(startAddress + verified, size)
            val expectedChunk = expected.copyOfRange(verified, verified + size)
            if (!actual.contentEquals(expectedChunk)) {
                throw StLinkException("回读校验失败: offset=$verified")
            }
            verified += size
            reportProgress(
                progressListener = progressListener,
                stage = Stm32FlashStage.VERIFYING,
                overallPercent = 80.0 + (verified.toDouble() / expected.size.toDouble() * 15.0),
                stageCompletedBytes = verified.toLong(),
                stageTotalBytes = expected.size.toLong(),
                message = "已校验 $verified / ${expected.size} 字节",
            )
        }
    }

    private fun readMemoryAligned(
        address: Long,
        size: Int,
    ): ByteArray {
        val alignedAddress = address and 0xFFFF_FFFCL
        val leadingSkip = (address - alignedAddress).toInt()
        val alignedSize = roundUpToMultipleOf4(leadingSkip + size)
        return session.readMemory32(alignedAddress, alignedSize)
            .copyOfRange(leadingSkip, leadingSkip + size)
    }

    internal fun reportProgress(
        progressListener: Stm32FlashProgressListener,
        stage: Stm32FlashStage,
        overallPercent: Double,
        stageCompletedBytes: Long,
        stageTotalBytes: Long,
        message: String,
    ) {
        val stagePercent =
            if (stageTotalBytes <= 0) {
                if (overallPercent >= 100.0) 100.0 else 0.0
            } else {
                (stageCompletedBytes.toDouble() / stageTotalBytes.toDouble() * 100.0).coerceIn(0.0, 100.0)
            }
        progressListener.onProgress(
            Stm32FlashProgress(
                stage = stage,
                stageCompletedBytes = stageCompletedBytes,
                stageTotalBytes = stageTotalBytes,
                overallPercent = overallPercent.coerceIn(0.0, 100.0),
                stagePercent = stagePercent,
                message = message,
            ),
        )
    }

    private fun attachTarget() {
        session.enterSwd()
        if (config.connectUnderReset) {
            session.driveReset(asserted = true)
            session.forceDebug()
            sleep(config.resetHoldTimeMs)
            session.driveReset(asserted = false)
            var attached = false
            for (attempt in 0 until config.attachRetryCount) {
                session.forceDebug()
                val dhcsr = session.readDebug32(STM32_REG_DHCSR)
                if ((dhcsr and STM32_REG_DHCSR_C_DEBUGEN.toLong()) != 0L) {
                    attached = true
                    break
                }
                if (attempt + 1 < config.attachRetryCount) {
                    sleep(config.attachRetryDelayMs)
                }
            }
            if (!attached) {
                throw StLinkException("connect-under-reset 后未能进入调试状态")
            }
        } else {
            session.forceDebug()
        }
    }

    private fun sleep(durationMs: Long) {
        if (durationMs > 0) {
            Thread.sleep(durationMs)
        }
    }

    companion object {
        /**
         * 枚举当前宿主可见的 ST-Link 设备。
         */
        fun listProbes(): List<StLinkProbeInfo> = StLinkUsbSession.listProbes()
    }
}

private class StLinkUsbSession private constructor(
    private val context: LibUsbContext,
    private val handle: LibUsbDeviceHandle,
    private val interfaceNumber: Int,
    val probeInfo: StLinkProbeInfo,
    initialRequestEndpoints: List<Byte>,
    initialReplyEndpoints: List<Byte>,
) : Closeable {
    private val requestEndpoints = initialRequestEndpoints.distinct()
    private val replyEndpoints = initialReplyEndpoints.distinct()
    private var epRep: Byte = replyEndpoints.first()
    private var epReq: Byte = requestEndpoints.first()
    private val maxWrite8Payload: Int = if (isV3Product(probeInfo.productId)) 512 else 64
    private var closed = false
    private var cachedVersion: StLinkVersion? = null

    fun readVersion(): StLinkVersion {
        cachedVersion?.let { version ->
            return version
        }
        val version =
            runCatching {
                readVersionInternal()
            }.getOrElse { throwable ->
                if (throwable is StLinkException && currentMode() == StLinkCurrentMode.Dfu) {
                    exitDfuMode()
                    readVersionInternal()
                } else {
                    throw throwable
                }
            }
        cachedVersion = version
        return version
    }

    fun readTargetVoltageMillivolts(): Int {
        ensureDebugCapableMode()
        val response = transferCommand(
            payload = byteArrayOf(STLINK_GET_TARGET_VOLTAGE.toByte()),
            responseSize = 8,
            responseName = "GET_TARGET_VOLTAGE",
        )
        val factor = response.readLeUInt32(0)
        val reading = response.readLeUInt32(4)
        if (factor == 0L || reading == 0L) {
            return 0
        }
        return ((2400L * reading) / factor).toInt()
    }

    fun currentMode(): StLinkCurrentMode {
        val response = transferCommand(
            payload = byteArrayOf(STLINK_GET_CURRENT_MODE.toByte()),
            responseSize = 2,
            responseName = "GET_CURRENT_MODE",
        )
        return StLinkCurrentMode.fromRawValue(response[0].toUnsignedByteValue())
    }

    fun enterSwd() {
        ensureDebugCapableMode()
        val version = readVersion()
        val enterCommand =
            when (version.jtagApiVersion) {
                StLinkJtagApiVersion.V1 -> STLINK_DEBUG_APIV1_ENTER
                StLinkJtagApiVersion.V2, StLinkJtagApiVersion.V3 -> STLINK_DEBUG_APIV2_ENTER
            }
        transferCommand(
            payload = byteArrayOf(
                STLINK_DEBUG_COMMAND.toByte(),
                enterCommand.toByte(),
                STLINK_DEBUG_ENTER_SWD.toByte(),
            ),
            responseSize = if (version.jtagApiVersion == StLinkJtagApiVersion.V1) 0 else 2,
            checkStatus = version.jtagApiVersion != StLinkJtagApiVersion.V1,
            retryOnWait = true,
            responseName = "ENTER_SWD",
        )
    }

    fun exitDfuMode() {
        transferCommand(
            payload = byteArrayOf(
                STLINK_DFU_COMMAND.toByte(),
                STLINK_DFU_EXIT.toByte(),
            ),
            responseSize = 0,
            responseName = "DFU_EXIT",
        )
        Thread.sleep(100)
    }

    private fun readVersionInternal(): StLinkVersion =
        if (isV3Product(probeInfo.productId)) {
            parseVersionV3(
                transferCommand(
                    payload = byteArrayOf(STLINK_GET_VERSION_APIV3.toByte()),
                    responseSize = 12,
                    responseName = "GET_VERSION_APIV3",
                ),
            )
        } else {
            parseVersionV2(
                transferCommand(
                    payload = byteArrayOf(STLINK_GET_VERSION.toByte()),
                    responseSize = 6,
                    responseName = "GET_VERSION",
                ),
            )
        }

    fun readCoreId(): Long {
        val response = transferCommand(
            payload = byteArrayOf(
                STLINK_DEBUG_COMMAND.toByte(),
                STLINK_DEBUG_APIV2_READ_IDCODES.toByte(),
            ),
            responseSize = 12,
            checkStatus = true,
            responseName = "READ_IDCODES",
        )
        return response.readLeUInt32(4)
    }

    fun readDebug32(address: Long): Long {
        val response = transferCommand(
            payload = byteArrayOf(
                STLINK_DEBUG_COMMAND.toByte(),
                STLINK_DEBUG_APIV2_READDEBUGREG.toByte(),
            ) + address.toLeBytes(),
            responseSize = 8,
            checkStatus = true,
            retryOnWait = true,
            responseName = "READDEBUGREG",
        )
        return response.readLeUInt32(4)
    }

    fun writeDebug32(
        address: Long,
        value: Long,
    ) {
        transferCommand(
            payload = byteArrayOf(
                STLINK_DEBUG_COMMAND.toByte(),
                STLINK_DEBUG_APIV2_WRITEDEBUGREG.toByte(),
            ) + address.toLeBytes() + value.toLeBytes(),
            responseSize = 2,
            checkStatus = true,
            retryOnWait = true,
            responseName = "WRITEDEBUGREG",
        )
    }

    fun readCoreRegister(index: Int): Long {
        require(index in 0..20) {
            "寄存器索引必须在 0..20"
        }
        val response = transferCommand(
            payload = byteArrayOf(
                STLINK_DEBUG_COMMAND.toByte(),
                STLINK_DEBUG_APIV2_READREG.toByte(),
                index.toByte(),
            ),
            responseSize = 8,
            checkStatus = true,
            retryOnWait = true,
            responseName = "READREG",
        )
        return response.readLeUInt32(4)
    }

    fun writeCoreRegister(
        index: Int,
        value: Long,
    ) {
        require(index in 0..20) {
            "寄存器索引必须在 0..20"
        }
        transferCommand(
            payload = byteArrayOf(
                STLINK_DEBUG_COMMAND.toByte(),
                STLINK_DEBUG_APIV2_WRITEREG.toByte(),
                index.toByte(),
            ) + value.toLeBytes(),
            responseSize = 2,
            checkStatus = true,
            retryOnWait = true,
            responseName = "WRITEREG",
        )
    }

    fun forceDebug() {
        writeDebug32(
            address = STM32_REG_DHCSR,
            value = STM32_REG_DHCSR_DBGKEY or STM32_REG_DHCSR_C_HALT or STM32_REG_DHCSR_C_DEBUGEN,
        )
    }

    fun releaseDebugHalt() {
        writeDebug32(
            address = STM32_REG_DHCSR,
            value = STM32_REG_DHCSR_DBGKEY or STM32_REG_DHCSR_C_DEBUGEN,
        )
    }

    fun runCore(maskInterrupts: Boolean) {
        writeDebug32(
            address = STM32_REG_DHCSR,
            value =
                STM32_REG_DHCSR_DBGKEY or
                    STM32_REG_DHCSR_C_DEBUGEN or
                    if (maskInterrupts) STM32_REG_DHCSR_C_MASKINTS else 0L,
        )
    }

    fun isCoreHalted(): Boolean =
        (readDebug32(STM32_REG_DHCSR) and STM32_REG_DHCSR_S_HALT.toLong()) != 0L

    fun resetSystem() {
        transferCommand(
            payload = byteArrayOf(
                STLINK_DEBUG_COMMAND.toByte(),
                STLINK_DEBUG_APIV2_RESETSYS.toByte(),
            ),
            responseSize = 2,
            checkStatus = true,
            retryOnWait = true,
            responseName = "RESETSYS",
        )
    }

    fun driveReset(asserted: Boolean) {
        transferCommand(
            payload = byteArrayOf(
                STLINK_DEBUG_COMMAND.toByte(),
                STLINK_DEBUG_APIV2_DRIVE_NRST.toByte(),
                if (asserted) STLINK_DEBUG_APIV2_DRIVE_NRST_LOW.toByte() else STLINK_DEBUG_APIV2_DRIVE_NRST_HIGH.toByte(),
            ),
            responseSize = 2,
            checkStatus = true,
            retryOnWait = true,
            responseName = "DRIVE_NRST",
        )
    }

    fun softResetAndHalt() {
        writeDebug32(
            address = STM32_REG_DHCSR,
            value = STM32_REG_DHCSR_DBGKEY or STM32_REG_DHCSR_C_HALT or STM32_REG_DHCSR_C_DEBUGEN,
        )
        writeDebug32(
            address = STM32_REG_CM3_DEMCR,
            value = STM32_REG_CM3_DEMCR_TRCENA or
                STM32_REG_CM3_DEMCR_VC_HARDERR or
                STM32_REG_CM3_DEMCR_VC_BUSERR or
                STM32_REG_CM3_DEMCR_VC_CORERESET,
        )
        writeDebug32(
            address = STM32_REG_DFSR,
            value = STM32_REG_DFSR_VCATCH,
        )
        readDebug32(STM32_REG_DHCSR)
        writeDebug32(
            address = STM32_REG_AIRCR,
            value = STM32_REG_AIRCR_VECTKEY or STM32_REG_AIRCR_SYSRESETREQ,
        )
        val deadline = System.nanoTime() + 500_000_000L
        while (System.nanoTime() < deadline) {
            val dhcsr = readDebug32(STM32_REG_DHCSR)
            if ((dhcsr and STM32_REG_DHCSR_S_RESET_ST) == 0L) {
                val dfsr = readDebug32(STM32_REG_DFSR)
                if ((dfsr and STM32_REG_DFSR_VCATCH) != 0L) {
                    break
                }
            }
            Thread.sleep(10)
        }
        writeDebug32(
            address = STM32_REG_DFSR,
            value = STM32_REG_DFSR_CLEAR,
        )
    }

    fun readMemory32(
        address: Long,
        size: Int,
    ): ByteArray {
        require(size in 1..0xFFFF) {
            "ST-Link 单次 READMEM_32BIT 的 size 必须在 1..65535"
        }
        return transferCommand(
            payload = byteArrayOf(
                STLINK_DEBUG_COMMAND.toByte(),
                STLINK_DEBUG_READMEM_32BIT.toByte(),
            ) + address.toLeBytes() + size.toLeUInt16Bytes(),
            responseSize = size,
            responseName = "READMEM_32BIT",
        )
    }

    fun writeMemory32(
        address: Long,
        data: ByteArray,
    ) {
        require(data.size in 1..0xFFFF) {
            "ST-Link 单次 WRITEMEM_32BIT 的 size 必须在 1..65535"
        }
        val command = byteArrayOf(
            STLINK_DEBUG_COMMAND.toByte(),
            STLINK_DEBUG_WRITEMEM_32BIT.toByte(),
        ) + address.toLeBytes() + data.size.toLeUInt16Bytes()
        bulkOut(padCommand(command), "WRITEMEM_32BIT/CMD")
        bulkOut(data, "WRITEMEM_32BIT/DATA")
        getLastReadWriteStatus()
    }

    fun writeMemory8(
        address: Long,
        data: ByteArray,
    ) {
        require(data.size <= maxWrite8Payload) {
            "当前 ST-Link 单次 WRITEMEM_8BIT 最多支持 $maxWrite8Payload bytes"
        }
        val command = byteArrayOf(
            STLINK_DEBUG_COMMAND.toByte(),
            STLINK_DEBUG_WRITEMEM_8BIT.toByte(),
        ) + address.toLeBytes() + data.size.toLeUInt16Bytes()
        bulkOut(padCommand(command), "WRITEMEM_8BIT/CMD")
        bulkOut(data, "WRITEMEM_8BIT/DATA")
    }

    fun unlockFlash(profile: Stm32StLinkFlashProfile) {
        val control = readDebug32(profile.flashControlRegisterAddress)
        if (!control.hasBit(FLASH_CR_LOCK_BIT)) {
            return
        }
        writeDebug32(profile.flashKeyRegisterAddress, FLASH_KEY1)
        writeDebug32(profile.flashKeyRegisterAddress, FLASH_KEY2)
        val unlockedControl = readDebug32(profile.flashControlRegisterAddress)
        if (unlockedControl.hasBit(FLASH_CR_LOCK_BIT)) {
            throw StLinkException("Flash 解锁失败")
        }
    }

    fun lockFlash(profile: Stm32StLinkFlashProfile) {
        val control = readDebug32(profile.flashControlRegisterAddress)
        if (!control.hasBit(FLASH_CR_LOCK_BIT)) {
            writeDebug32(
                profile.flashControlRegisterAddress,
                control.setBit(FLASH_CR_LOCK_BIT),
            )
        }
    }

    fun clearFlashStatus(profile: Stm32StLinkFlashProfile) {
        writeDebug32(profile.flashStatusRegisterAddress, FLASH_SR_CLEAR_MASK)
    }

    fun waitFlashReady(profile: Stm32StLinkFlashProfile) {
        val deadline = System.nanoTime() + FLASH_OPERATION_TIMEOUT_NS
        while (System.nanoTime() < deadline) {
            if (!readDebug32(profile.flashStatusRegisterAddress).hasBit(FLASH_SR_BSY_BIT)) {
                return
            }
            Thread.sleep(1)
        }
        throw StLinkException("等待 Flash 空闲超时")
    }

    fun assertFlashOperationSucceeded(
        profile: Stm32StLinkFlashProfile,
        operationName: String,
    ) {
        val status = readDebug32(profile.flashStatusRegisterAddress)
        val problems = buildList {
            if (status.hasBit(FLASH_SR_PG_ERR_BIT)) {
                add("PG_ERR")
            }
            if (status.hasBit(FLASH_SR_WRPRT_ERR_BIT)) {
                add("WRPRT_ERR")
            }
        }
        if (problems.isNotEmpty()) {
            throw StLinkException("$operationName 失败: ${problems.joinToString(", ")}")
        }
    }

    fun prepareFlashLoader(
        profile: Stm32StLinkFlashProfile,
        geometry: Stm32StLinkFlashGeometry,
    ): Stm32FlashLoaderSession {
        forceDebug()
        writeDebug32(
            address = STM32_REG_DHCSR,
            value =
                STM32_REG_DHCSR_DBGKEY or
                    STM32_REG_DHCSR_C_DEBUGEN or
                    STM32_REG_DHCSR_C_HALT or
                    STM32_REG_DHCSR_C_MASKINTS,
        )
        clearDebugFaultStatus()
        writeMemory32(geometry.sramBaseAddress, profile.loaderCode)
        return Stm32FlashLoaderSession(
            loaderAddress = geometry.sramBaseAddress,
            bufferAddress = geometry.sramBaseAddress + profile.loaderCode.size,
        )
    }

    fun runFlashLoaderChunk(
        loader: Stm32FlashLoaderSession,
        profile: Stm32StLinkFlashProfile,
        targetAddress: Long,
        payload: ByteArray,
    ) {
        val flashPayload = payload.padToEvenBytes(0xFF.toByte())
        val sramPayload = flashPayload.padToMultipleOf4(0xFF.toByte())
        clearDebugFaultStatus()
        writeMemory32(loader.bufferAddress, sramPayload)
        writeCoreRegister(0, loader.bufferAddress)
        writeCoreRegister(1, targetAddress)
        writeCoreRegister(2, flashPayload.size.toLong())
        writeCoreRegister(3, profile.loaderFlashRegisterBaseOffset)
        writeCoreRegister(15, loader.loaderAddress)
        runCore(maskInterrupts = true)
        val deadline = System.nanoTime() + FLASH_OPERATION_TIMEOUT_NS
        while (System.nanoTime() < deadline) {
            if (isCoreHalted()) {
                val remain = readCoreRegister(2).toInt()
                if (remain > 0 || remain < -7) {
                    throw StLinkException("Flash loader 执行失败: remain=$remain target=0x${targetAddress.toString(16).uppercase()}")
                }
                assertFlashOperationSucceeded(profile, "写入")
                clearDebugFaultStatus()
                return
            }
            Thread.sleep(10)
        }
        forceDebug()
        throw StLinkException("Flash loader 执行超时: target=0x${targetAddress.toString(16).uppercase()}")
    }

    private fun clearDebugFaultStatus() {
        writeDebug32(STM32_REG_DFSR, STM32_REG_DFSR_CLEAR)
        writeDebug32(STM32_REG_CFSR, MAX_U32_VALUE)
        writeDebug32(STM32_REG_HFSR, MAX_U32_VALUE)
    }

    override fun close() {
        if (closed) {
            return
        }
        closed = true
        runCatching {
            LibUsb.native.libusb_release_interface(handle, interfaceNumber)
        }
        runCatching {
            LibUsb.native.libusb_close(handle)
        }
        runCatching {
            LibUsb.native.libusb_exit(context)
        }
    }

    private fun getLastReadWriteStatus() {
        val version = readVersion()
        val hasStatus2 = version.hasGetLastReadWriteStatus2
        transferCommand(
            payload = byteArrayOf(
                STLINK_DEBUG_COMMAND.toByte(),
                if (hasStatus2) STLINK_DEBUG_APIV2_GETLASTRWSTATUS2.toByte() else STLINK_DEBUG_APIV2_GETLASTRWSTATUS.toByte(),
            ),
            responseSize = if (hasStatus2) 12 else 2,
            checkStatus = true,
            responseName = if (hasStatus2) "GETLASTRWSTATUS2" else "GETLASTRWSTATUS",
        )
    }

    private fun ensureDebugCapableMode() {
        if (currentMode() == StLinkCurrentMode.Dfu) {
            exitDfuMode()
        }
    }

    private fun transferCommand(
        payload: ByteArray,
        responseSize: Int,
        checkStatus: Boolean = false,
        retryOnWait: Boolean = false,
        responseName: String,
    ): ByteArray {
        var lastError: Throwable? = null
        repeat(if (retryOnWait) 4 else 1) { attempt ->
            try {
                bulkOut(padCommand(payload), "$responseName/CMD")
                if (responseSize == 0) {
                    return ByteArray(0)
                }
                val response = bulkIn(responseSize, responseName)
                if (checkStatus) {
                    validateStatus(response, responseName)
                }
                return response
            } catch (throwable: Throwable) {
                lastError = throwable
                if (!retryOnWait || throwable !is StLinkException || !throwable.message.orEmpty().contains("WAIT")) {
                    throw throwable
                }
                if (attempt < 3) {
                    Thread.sleep((1 shl attempt) * 1L)
                }
            }
        }
        throw StLinkException("$responseName 执行失败", lastError)
    }

    private fun bulkOut(
        data: ByteArray,
        opName: String,
    ) {
        val endpointsToTry = listOf(epReq) + requestEndpoints.filterNot { endpoint -> endpoint == epReq }
        var lastError: StLinkException? = null
        for (index in endpointsToTry.indices) {
            val endpoint = endpointsToTry[index]
            val actualLength = IntByReference()
            val result = LibUsb.native.libusb_bulk_transfer(
                handle,
                endpoint,
                data,
                data.size,
                actualLength,
                USB_TIMEOUT_MS,
            )
            if (result == 0 && actualLength.value == data.size) {
                epReq = endpoint
                return
            }
            if (result == 0) {
                lastError = StLinkException("$opName 发送长度异常: expected=${data.size}, actual=${actualLength.value}")
            } else {
                lastError = StLinkException("$opName 发送失败: ${libusbErrorName(result)}")
            }
            if (result == LIBUSB_ERROR_PIPE || result == LIBUSB_ERROR_TIMEOUT) {
                LibUsb.native.libusb_clear_halt(handle, endpoint)
            }
            if ((result != LIBUSB_ERROR_PIPE && result != LIBUSB_ERROR_TIMEOUT) || index == endpointsToTry.lastIndex) {
                break
            }
        }
        throw lastError ?: StLinkException("$opName 发送失败")
    }

    private fun bulkIn(
        expectedSize: Int,
        opName: String,
    ): ByteArray {
        val endpointsToTry = listOf(epRep) + replyEndpoints.filterNot { endpoint -> endpoint == epRep }
        var lastError: StLinkException? = null
        for (index in endpointsToTry.indices) {
            val endpoint = endpointsToTry[index]
            val buffer = ByteArray(expectedSize)
            val actualLength = IntByReference()
            val result = LibUsb.native.libusb_bulk_transfer(
                handle,
                endpoint,
                buffer,
                buffer.size,
                actualLength,
                USB_TIMEOUT_MS,
            )
            if (result == 0 && actualLength.value == expectedSize) {
                epRep = endpoint
                return buffer
            }
            lastError =
                if (result != 0) {
                    StLinkException("$opName 接收失败: ${libusbErrorName(result)}")
                } else {
                    StLinkException("$opName 返回长度异常: expected=$expectedSize, actual=${actualLength.value}")
                }
            if (result == LIBUSB_ERROR_PIPE || result == LIBUSB_ERROR_TIMEOUT) {
                LibUsb.native.libusb_clear_halt(handle, endpoint)
            }
            if ((result != LIBUSB_ERROR_PIPE && result != LIBUSB_ERROR_TIMEOUT) || index == endpointsToTry.lastIndex) {
                break
            }
        }
        throw lastError ?: StLinkException("$opName 接收失败")
    }

    private fun validateStatus(
        response: ByteArray,
        opName: String,
    ) {
        val status = response.firstOrNull()?.toUnsignedByteValue()
            ?: throw StLinkException("$opName 返回空响应")
        if (status == STLINK_DEBUG_ERR_OK) {
            return
        }
        val reason =
            when (status) {
                STLINK_DEBUG_ERR_AP_WAIT, STLINK_DEBUG_ERR_DP_WAIT -> "WAIT"
                STLINK_DEBUG_ERR_FAULT -> "FAULT"
                STLINK_DEBUG_ERR_AP_FAULT -> "AP_FAULT"
                STLINK_DEBUG_ERR_DP_FAULT -> "DP_FAULT"
                STLINK_DEBUG_ERR_AP_ERROR -> "AP_ERROR"
                STLINK_DEBUG_ERR_DP_ERROR -> "DP_ERROR"
                STLINK_DEBUG_ERR_WRITE_VERIFY -> "WRITE_VERIFY"
                STLINK_DEBUG_ERR_WRITE -> "WRITE"
                else -> "0x${status.toString(16).uppercase()}"
            }
        throw StLinkException("$opName 失败: $reason")
    }

    private fun libusbErrorName(errorCode: Int): String {
        val pointer = LibUsb.native.libusb_error_name(errorCode)
        if (pointer.isNullPointer()) {
            return "code=$errorCode"
        }
        return pointer!!.getString(0)
    }

    companion object {
        fun listProbes(): List<StLinkProbeInfo> =
            withLibUsbContext { context ->
                useDeviceList(context) { devicePointers ->
                    devicePointers.mapNotNull { devicePointer ->
                        readProbeInfo(devicePointer)
                    }
                }
            }

        fun open(config: StLinkConfig): StLinkUsbSession {
            val contextRef = PointerByReference()
            val initResult = LibUsb.native.libusb_init(contextRef)
            if (initResult != 0) {
                throw StLinkException("libusb_init 失败: $initResult")
            }
            val context = LibUsbContext(contextRef.value)
            return try {
                val probes = mutableListOf<OpenedProbe>()
                useDeviceList(context) { devicePointers ->
                    devicePointers.forEach { devicePointer ->
                        val opened = openProbe(devicePointer)
                        if (opened != null) {
                            probes += opened
                        }
                    }
                }
                val matched =
                    if (config.serialNumber.isNullOrBlank()) {
                        probes
                    } else {
                        probes.filter { openedProbe -> openedProbe.probeInfo.serialNumber == config.serialNumber }
                    }
                if (matched.isEmpty()) {
                    probes.forEach { openedProbe -> LibUsb.native.libusb_close(openedProbe.handle) }
                    throw StLinkException("未找到匹配的 ST-Link 设备")
                }
                if (matched.size > 1) {
                    probes.forEach { openedProbe -> LibUsb.native.libusb_close(openedProbe.handle) }
                    throw StLinkException("检测到多块 ST-Link，请通过 serialNumber 指定要连接的探针")
                }
                val openedProbe = matched.single()
                probes.filterNot { probe -> probe === openedProbe }.forEach { probe ->
                    LibUsb.native.libusb_close(probe.handle)
                }
                try {
                    claimInterface(openedProbe.handle, openedProbe.endpointPlan.interfaceNumber)
                } catch (throwable: Throwable) {
                    LibUsb.native.libusb_close(openedProbe.handle)
                    throw throwable
                }
                StLinkUsbSession(
                    context = context,
                    handle = openedProbe.handle,
                    interfaceNumber = openedProbe.endpointPlan.interfaceNumber,
                    probeInfo = openedProbe.probeInfo,
                    initialRequestEndpoints = openedProbe.endpointPlan.requestEndpoints,
                    initialReplyEndpoints = openedProbe.endpointPlan.replyEndpoints,
                )
            } catch (throwable: Throwable) {
                runCatching {
                    LibUsb.native.libusb_exit(context)
                }
                throw throwable
            }
        }

        private fun readProbeInfo(devicePointer: LibUsbDevice): StLinkProbeInfo? {
            val opened = openProbe(devicePointer) ?: return null
            LibUsb.native.libusb_close(opened.handle)
            return opened.probeInfo
        }

        private fun openProbe(devicePointer: LibUsbDevice): OpenedProbe? {
            val descriptor = LibUsbDeviceDescriptor()
            val descriptorResult = LibUsb.native.libusb_get_device_descriptor(devicePointer, descriptor)
            if (descriptorResult != 0) {
                return null
            }
            if (descriptor.vendorId() != STLINK_VENDOR_ID || !isSupportedProduct(descriptor.productId())) {
                return null
            }
            val handleRef = PointerByReference()
            val openResult = LibUsb.native.libusb_open(devicePointer, handleRef)
            if (openResult != 0 || handleRef.value.isNullPointer()) {
                return null
            }
            val handle = LibUsbDeviceHandle(handleRef.value)
            val endpointPlan = resolveEndpointPlan(devicePointer, descriptor.productId())
            if (endpointPlan == null) {
                LibUsb.native.libusb_close(handle)
                return null
            }
            val probeInfo =
                StLinkProbeInfo(
                    vendorId = descriptor.vendorId(),
                    productId = descriptor.productId(),
                    serialNumber = descriptor.readIndexedAsciiString(handle, descriptor.iSerialNumber),
                    productName = descriptor.readIndexedAsciiString(handle, descriptor.iProduct),
                    manufacturerName = descriptor.readIndexedAsciiString(handle, descriptor.iManufacturer),
                )
            return OpenedProbe(
                probeInfo = probeInfo,
                handle = handle,
                endpointPlan = endpointPlan,
            )
        }

        private fun claimInterface(
            handle: LibUsbDeviceHandle,
            interfaceNumber: Int,
        ) {
            val configRef = IntByReference()
            val getConfigResult = LibUsb.native.libusb_get_configuration(handle, configRef)
            if (getConfigResult == 0 && configRef.value != 1) {
                val setConfigResult = LibUsb.native.libusb_set_configuration(handle, 1)
                if (setConfigResult != 0) {
                    throw StLinkException("无法把 ST-Link USB configuration 切到 1: $setConfigResult")
                }
            }
            val claimResult = LibUsb.native.libusb_claim_interface(handle, interfaceNumber)
            if (claimResult != 0) {
                throw StLinkException("无法 claim ST-Link interface $interfaceNumber: $claimResult")
            }
        }

        private fun withLibUsbContext(block: (LibUsbContext) -> List<StLinkProbeInfo>): List<StLinkProbeInfo> {
            val contextRef = PointerByReference()
            val initResult = LibUsb.native.libusb_init(contextRef)
            if (initResult != 0) {
                throw StLinkException("libusb_init 失败: $initResult")
            }
            val context = LibUsbContext(contextRef.value)
            return try {
                block(context)
            } finally {
                LibUsb.native.libusb_exit(context)
            }
        }

        private fun <T> useDeviceList(
            context: LibUsbContext,
            block: (List<LibUsbDevice>) -> T,
        ): T {
            val listRef = PointerByReference()
            val count = LibUsb.native.libusb_get_device_list(context, listRef)
            if (count < 0) {
                throw StLinkException("libusb_get_device_list 失败: $count")
            }
            val listPointer = listRef.value
                ?: throw StLinkException("libusb_get_device_list 返回了空指针")
            return try {
                val devices =
                    buildList {
                        for (index in 0 until count.toInt()) {
                            val pointer = listPointer.pointerAt(index) ?: break
                            if (pointer.isNullPointer()) {
                                break
                            }
                            add(LibUsbDevice(pointer))
                        }
                    }
                block(devices)
            } finally {
                LibUsb.native.libusb_free_device_list(listPointer, 1)
            }
        }

        private fun resolveEndpointPlan(
            device: LibUsbDevice,
            probeProductId: Int,
        ): StLinkEndpointPlan? {
            val configRef = PointerByReference()
            val result = LibUsb.native.libusb_get_active_config_descriptor(device, configRef)
            if (result != 0 || configRef.value.isNullPointer()) {
                return null
            }
            val configPointer = configRef.value!!
            return try {
                val configDescriptor = LibUsbConfigDescriptor(configPointer)
                val interfaceSize = LibUsbInterface().size()
                val interfaceDescriptorSize = LibUsbInterfaceDescriptor().size()
                val endpointDescriptorSize = LibUsbEndpointDescriptor().size()
                val interfaceCount = configDescriptor.bNumInterfaces.toUnsignedByteValue()
                for (interfaceIndex in 0 until interfaceCount) {
                    val interfacePointer = configDescriptor.iface?.share(interfaceIndex.toLong() * interfaceSize) ?: continue
                    val usbInterface = LibUsbInterface(interfacePointer)
                    for (altIndex in 0 until usbInterface.num_altsetting) {
                        val descriptorPointer = usbInterface.altsetting?.share(altIndex.toLong() * interfaceDescriptorSize) ?: continue
                        val interfaceDescriptor = LibUsbInterfaceDescriptor(descriptorPointer)
                        val requestEndpoints = mutableListOf<Byte>()
                        val replyEndpoints = mutableListOf<Byte>()
                        val endpointCount = interfaceDescriptor.bNumEndpoints.toUnsignedByteValue()
                        for (endpointIndex in 0 until endpointCount) {
                            val endpointPointer = interfaceDescriptor.endpoint?.share(endpointIndex.toLong() * endpointDescriptorSize) ?: continue
                            val endpointDescriptor = LibUsbEndpointDescriptor(endpointPointer)
                            val attributes = endpointDescriptor.bmAttributes.toUnsignedByteValue() and 0x03
                            if (attributes != LIBUSB_TRANSFER_TYPE_BULK) {
                                continue
                            }
                            val endpointAddress = endpointDescriptor.bEndpointAddress
                            if ((endpointAddress.toUnsignedByteValue() and 0x80) != 0) {
                                replyEndpoints += endpointAddress
                            } else {
                                requestEndpoints += endpointAddress
                            }
                        }
                        if (requestEndpoints.isNotEmpty() && replyEndpoints.isNotEmpty()) {
                            val preferredRequestEndpoint = selectRequestEndpointForProduct(requestEndpoints, probeProductId)
                            val preferredReplyEndpoint = selectReplyEndpoint(replyEndpoints)
                            return StLinkEndpointPlan(
                                interfaceNumber = interfaceDescriptor.bInterfaceNumber.toUnsignedByteValue(),
                                requestEndpoints = listOf(preferredRequestEndpoint),
                                replyEndpoints = listOf(preferredReplyEndpoint),
                            )
                        }
                    }
                }
                null
            } finally {
                LibUsb.native.libusb_free_config_descriptor(configPointer)
            }
        }
    }
}

private data class OpenedProbe(
    val probeInfo: StLinkProbeInfo,
    val handle: LibUsbDeviceHandle,
    val endpointPlan: StLinkEndpointPlan,
)

private data class StLinkEndpointPlan(
    val interfaceNumber: Int,
    val requestEndpoints: List<Byte>,
    val replyEndpoints: List<Byte>,
)

internal data class Stm32StLinkFlashProfile(
    val chipId: Int,
    val flashBaseAddress: Long,
    val flashSizeRegisterAddress: Long,
    val defaultFlashSizeBytes: Int?,
    val pageSizeBytes: Int,
    val sramBaseAddress: Long,
    val sramSizeBytes: Int,
    val flashKeyRegisterAddress: Long,
    val flashStatusRegisterAddress: Long,
    val flashControlRegisterAddress: Long,
    val flashAddressRegisterAddress: Long,
    val loaderFlashRegisterBaseOffset: Long,
    val loaderCode: ByteArray,
)

private data class Stm32FlashLoaderSession(
    val loaderAddress: Long,
    val bufferAddress: Long,
)

private const val STLINK_VENDOR_ID = 0x0483
private const val STLINK_USB_PID_STLINK = 0x3744
private const val STLINK_USB_PID_STLINK_32L = 0x3748
private const val STLINK_USB_PID_STLINK_32L_AUDIO = 0x374A
private const val STLINK_USB_PID_STLINK_NUCLEO = 0x374B
private const val STLINK_USB_PID_STLINK_V2_1 = 0x3752
private const val STLINK_USB_PID_STLINK_V3_USBLOADER = 0x374D
private const val STLINK_USB_PID_STLINK_V3E = 0x374E
private const val STLINK_USB_PID_STLINK_V3S = 0x374F
private const val STLINK_USB_PID_STLINK_V3_2VCP = 0x3753
private const val STLINK_USB_PID_STLINK_V3_NO_MSD = 0x3754
private const val STLINK_USB_PID_STLINK_V3P = 0x3757

private const val STLINK_CMD_SIZE = 16
private const val STLINK_EP_REP: Byte = 0x81.toByte()
private const val USB_TIMEOUT_MS = 3_000
private const val LIBUSB_ERROR_TIMEOUT = -7
private const val LIBUSB_ERROR_PIPE = -9
private const val LIBUSB_TRANSFER_TYPE_BULK = 0x02

private const val STLINK_GET_VERSION = 0xF1
private const val STLINK_DEBUG_COMMAND = 0xF2
private const val STLINK_DFU_COMMAND = 0xF3
private const val STLINK_DFU_EXIT = 0x07
private const val STLINK_GET_CURRENT_MODE = 0xF5
private const val STLINK_GET_TARGET_VOLTAGE = 0xF7
private const val STLINK_GET_VERSION_APIV3 = 0xFB

private const val STLINK_DEBUG_READMEM_32BIT = 0x07
private const val STLINK_DEBUG_WRITEMEM_32BIT = 0x08
private const val STLINK_DEBUG_WRITEMEM_8BIT = 0x0D
private const val STLINK_DEBUG_APIV1_ENTER = 0x20
private const val STLINK_DEBUG_APIV2_ENTER = 0x30
private const val STLINK_DEBUG_APIV2_READ_IDCODES = 0x31
private const val STLINK_DEBUG_APIV2_RESETSYS = 0x32
private const val STLINK_DEBUG_APIV2_READREG = 0x33
private const val STLINK_DEBUG_APIV2_WRITEREG = 0x34
private const val STLINK_DEBUG_APIV2_WRITEDEBUGREG = 0x35
private const val STLINK_DEBUG_APIV2_READDEBUGREG = 0x36
private const val STLINK_DEBUG_APIV2_GETLASTRWSTATUS = 0x3B
private const val STLINK_DEBUG_APIV2_DRIVE_NRST = 0x3C
private const val STLINK_DEBUG_APIV2_GETLASTRWSTATUS2 = 0x3E
private const val STLINK_DEBUG_ENTER_SWD = 0xA3

private const val STLINK_DEBUG_APIV2_DRIVE_NRST_LOW = 0x00
private const val STLINK_DEBUG_APIV2_DRIVE_NRST_HIGH = 0x01

private const val STLINK_DEBUG_ERR_OK = 0x80
private const val STLINK_DEBUG_ERR_FAULT = 0x81
private const val STLINK_DEBUG_ERR_WRITE = 0x0C
private const val STLINK_DEBUG_ERR_WRITE_VERIFY = 0x0D
private const val STLINK_DEBUG_ERR_AP_WAIT = 0x10
private const val STLINK_DEBUG_ERR_AP_FAULT = 0x11
private const val STLINK_DEBUG_ERR_AP_ERROR = 0x12
private const val STLINK_DEBUG_ERR_DP_WAIT = 0x14
private const val STLINK_DEBUG_ERR_DP_FAULT = 0x15
private const val STLINK_DEBUG_ERR_DP_ERROR = 0x16

private const val STM32_REG_CM3_CPUID = 0xE000ED00L
private const val STM32_REG_CM3_DEMCR = 0xE000EDFCL
private const val STM32_REG_CM3_DEMCR_TRCENA = 1L shl 24
private const val STM32_REG_CM3_DEMCR_VC_HARDERR = 1L shl 10
private const val STM32_REG_CM3_DEMCR_VC_BUSERR = 1L shl 8
private const val STM32_REG_CM3_DEMCR_VC_CORERESET = 1L shl 0
private const val STM32_REG_DFSR = 0xE000ED30L
private const val STM32_REG_DFSR_VCATCH = 1L shl 3
private const val STM32_REG_DFSR_CLEAR = 0x1FL
private const val STM32_REG_CFSR = 0xE000ED28L
private const val STM32_REG_HFSR = 0xE000ED2CL
private const val STM32_REG_DHCSR = 0xE000EDF0L
private const val STM32_REG_DHCSR_DBGKEY = 0xA05F0000L
private const val STM32_REG_DHCSR_C_DEBUGEN = 1L shl 0
private const val STM32_REG_DHCSR_C_HALT = 1L shl 1
private const val STM32_REG_DHCSR_C_MASKINTS = 1L shl 3
private const val STM32_REG_DHCSR_S_HALT = 1L shl 17
private const val STM32_REG_DHCSR_S_RESET_ST = 1L shl 25
private const val STM32_REG_AIRCR = 0xE000ED0CL
private const val STM32_REG_AIRCR_VECTKEY = 0x05FA0000L
private const val STM32_REG_AIRCR_SYSRESETREQ = 0x00000004L

private const val FLASH_KEY1 = 0x45670123L
private const val FLASH_KEY2 = 0xCDEF89ABL
private const val FLASH_SR_BSY_BIT = 0
private const val FLASH_SR_PG_ERR_BIT = 2
private const val FLASH_SR_WRPRT_ERR_BIT = 4
private const val FLASH_SR_EOP_BIT = 5
private const val FLASH_SR_CLEAR_MASK =
    (1L shl FLASH_SR_PG_ERR_BIT) or
        (1L shl FLASH_SR_WRPRT_ERR_BIT) or
        (1L shl FLASH_SR_EOP_BIT)
private const val FLASH_CR_PG_BIT = 0
private const val FLASH_CR_PER_BIT = 1
private const val FLASH_CR_MER_BIT = 2
private const val FLASH_CR_STRT_BIT = 6
private const val FLASH_CR_LOCK_BIT = 7

private const val FLASH_OPERATION_TIMEOUT_NS = 5_000_000_000L

private const val CORTEX_M0_PARTNO = 0xC20
private const val CORTEX_M0P_PARTNO = 0xC60
private const val CORTEX_M7_PARTNO = 0xC27
private const val CORTEX_M33_PARTNO = 0xD21

private fun isSupportedProduct(productId: Int): Boolean =
    productId in supportedProductIds

private fun isV3Product(productId: Int): Boolean =
    productId in v3ProductIds

private fun selectRequestEndpointForProduct(
    endpoints: List<Byte>,
    productId: Int,
): Byte {
    val preferredEndpoint =
        if (
            productId == STLINK_USB_PID_STLINK_NUCLEO ||
            productId == STLINK_USB_PID_STLINK_32L_AUDIO ||
            productId == STLINK_USB_PID_STLINK_V2_1 ||
            productId in v3ProductIds
        ) {
            0x01
        } else {
            0x02
        }.toByte()
    return endpoints.firstOrNull { endpoint -> endpoint == preferredEndpoint } ?: endpoints.first()
}

private fun selectReplyEndpoint(endpoints: List<Byte>): Byte =
    endpoints.firstOrNull { endpoint -> endpoint == STLINK_EP_REP } ?: endpoints.first()

private fun parseVersionV2(response: ByteArray): StLinkVersion {
    val b0 = response[0].toUnsignedByteValue()
    val b1 = response[1].toUnsignedByteValue()
    val b2 = response[2].toUnsignedByteValue()
    val b3 = response[3].toUnsignedByteValue()
    val b4 = response[4].toUnsignedByteValue()
    val b5 = response[5].toUnsignedByteValue()
    val stlinkVersion = (b0 shr 4) and 0x0F
    val jtagVersion = ((b0 and 0x0F) shl 2) or ((b1 and 0xC0) shr 6)
    val swimVersion = b1 and 0x3F
    return StLinkVersion(
        stlinkVersion = stlinkVersion,
        jtagVersion = jtagVersion,
        swimVersion = swimVersion,
        vendorId = (b3 shl 8) or b2,
        productId = (b5 shl 8) or b4,
        jtagApiVersion = if (stlinkVersion <= 1 && jtagVersion <= 11) StLinkJtagApiVersion.V1 else StLinkJtagApiVersion.V2,
        hasGetLastReadWriteStatus2 = jtagVersion >= 15,
    )
}

private fun parseVersionV3(response: ByteArray): StLinkVersion =
    StLinkVersion(
        stlinkVersion = response[0].toUnsignedByteValue(),
        jtagVersion = response[2].toUnsignedByteValue(),
        swimVersion = response[1].toUnsignedByteValue(),
        vendorId = (response[9].toUnsignedByteValue() shl 8) or response[8].toUnsignedByteValue(),
        productId = (response[11].toUnsignedByteValue() shl 8) or response[10].toUnsignedByteValue(),
        jtagApiVersion = StLinkJtagApiVersion.V3,
        hasGetLastReadWriteStatus2 = true,
    )

internal fun resolveChipIdRegisterAddress(
    coreId: Long,
    cpuId: Long,
): Long {
    val partNumber = ((cpuId shr 4) and 0xFFF).toInt()
    return when (partNumber) {
        CORTEX_M0_PARTNO, CORTEX_M0P_PARTNO -> 0x40015800L
        CORTEX_M33_PARTNO -> 0xE0044000L
        CORTEX_M7_PARTNO -> {
            if (coreId == 0x6BA02477L || coreId == 0x6BA00477L) {
                0x5C001000L
            } else {
                0xE0042000L
            }
        }
        else -> 0xE0042000L
    }
}

private fun padCommand(payload: ByteArray): ByteArray {
    require(payload.size <= STLINK_CMD_SIZE) {
        "ST-Link 命令头不能超过 $STLINK_CMD_SIZE bytes"
    }
    return ByteArray(STLINK_CMD_SIZE).also { output ->
        payload.copyInto(output, endIndex = payload.size)
    }
}

private fun Long.toLeBytes(): ByteArray =
    byteArrayOf(
        (this and 0xFF).toByte(),
        ((this ushr 8) and 0xFF).toByte(),
        ((this ushr 16) and 0xFF).toByte(),
        ((this ushr 24) and 0xFF).toByte(),
    )

private fun Int.toLeUInt16Bytes(): ByteArray =
    byteArrayOf(
        (this and 0xFF).toByte(),
        ((this ushr 8) and 0xFF).toByte(),
    )

private fun ByteArray.readLeUInt32(offset: Int): Long =
    (this[offset].toUnsignedByteValue().toLong()) or
        (this[offset + 1].toUnsignedByteValue().toLong() shl 8) or
        (this[offset + 2].toUnsignedByteValue().toLong() shl 16) or
        (this[offset + 3].toUnsignedByteValue().toLong() shl 24)

private fun ByteArray.readLeUInt16(offset: Int): Int =
    this[offset].toUnsignedByteValue() or
        (this[offset + 1].toUnsignedByteValue() shl 8)

private fun ByteArray.padToEvenBytes(padByte: Byte): ByteArray =
    if (size % 2 == 0) {
        this
    } else {
        copyOf(size + 1).also { output ->
            output[output.lastIndex] = padByte
        }
    }

private fun ByteArray.padToMultipleOf4(padByte: Byte): ByteArray {
    val alignedSize = roundUpToMultipleOf4(size)
    if (alignedSize == size) {
        return this
    }
    return copyOf(alignedSize).also { output ->
        for (index in size until alignedSize) {
            output[index] = padByte
        }
    }
}

private fun roundUpToMultipleOf4(value: Int): Int =
    if (value % 4 == 0) {
        value
    } else {
        value + (4 - (value % 4))
    }

private fun Long.hasBit(bit: Int): Boolean = (this and (1L shl bit)) != 0L

private fun Long.setBit(bit: Int): Long = this or (1L shl bit)

private fun Long.clearBit(bit: Int): Long = this and (1L shl bit).inv()

private val supportedProductIds =
    setOf(
        STLINK_USB_PID_STLINK,
        STLINK_USB_PID_STLINK_32L,
        STLINK_USB_PID_STLINK_32L_AUDIO,
        STLINK_USB_PID_STLINK_NUCLEO,
        STLINK_USB_PID_STLINK_V2_1,
        STLINK_USB_PID_STLINK_V3_USBLOADER,
        STLINK_USB_PID_STLINK_V3E,
        STLINK_USB_PID_STLINK_V3S,
        STLINK_USB_PID_STLINK_V3_2VCP,
        STLINK_USB_PID_STLINK_V3_NO_MSD,
        STLINK_USB_PID_STLINK_V3P,
    )

private val v3ProductIds =
    setOf(
        STLINK_USB_PID_STLINK_V3_USBLOADER,
        STLINK_USB_PID_STLINK_V3E,
        STLINK_USB_PID_STLINK_V3S,
        STLINK_USB_PID_STLINK_V3_2VCP,
        STLINK_USB_PID_STLINK_V3_NO_MSD,
        STLINK_USB_PID_STLINK_V3P,
    )

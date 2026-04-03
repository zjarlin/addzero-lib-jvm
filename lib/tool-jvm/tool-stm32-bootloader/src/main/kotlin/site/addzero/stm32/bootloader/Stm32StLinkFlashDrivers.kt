package site.addzero.stm32.bootloader

/**
 * 不同芯片族的 ST-Link Flash 规程入口。
 *
 * 探针/USB 会话层和芯片 Flash 规程是两类变化频率不同的代码：
 * - 前者围绕 ST-Link 协议
 * - 后者围绕目标 MCU 的 Flash 控制器
 *
 * 因此这里按 driver 注册，避免后续继续把 `chipId` 分支堆回主类。
 */
internal interface Stm32StLinkFlashDriver {
    fun supports(chipId: Int): Boolean

    fun readGeometry(
        programmer: Stm32StLinkProgrammer,
        targetInfo: Stm32SwdTargetInfo,
    ): Stm32StLinkFlashGeometry

    fun flash(
        programmer: Stm32StLinkProgrammer,
        request: Stm32FlashRequest,
        targetInfo: Stm32SwdTargetInfo,
        geometry: Stm32StLinkFlashGeometry,
        progressListener: Stm32FlashProgressListener,
    ): Stm32StLinkFlashReport
}

internal fun resolveStm32StLinkFlashDriver(chipId: Int): Stm32StLinkFlashDriver =
    stLinkFlashDrivers.firstOrNull { driver -> driver.supports(chipId) }
        ?: throw StLinkException("当前暂未实现 chipId=0x${chipId.toString(16).uppercase()} 的 ST-Link Flash 写入")

private val stLinkFlashDrivers: List<Stm32StLinkFlashDriver> =
    listOf(
        Stm32F1HighDensityFlashDriver,
    )

private object Stm32F1HighDensityFlashDriver : Stm32StLinkFlashDriver {
    private val profile by lazy {
        Stm32StLinkFlashProfile(
            chipId = STM32_CHIPID_F1_HD,
            flashBaseAddress = STM32_FLASH_BASE,
            flashSizeRegisterAddress = STM32F1_FLASH_SIZE_REG,
            defaultFlashSizeBytes = 256 * 1024,
            pageSizeBytes = 0x800,
            sramBaseAddress = STM32_SRAM_BASE,
            sramSizeBytes = 0x10000,
            flashKeyRegisterAddress = STM32F1_FLASH_KEYR,
            flashStatusRegisterAddress = STM32F1_FLASH_SR,
            flashControlRegisterAddress = STM32F1_FLASH_CR,
            flashAddressRegisterAddress = STM32F1_FLASH_AR,
            loaderFlashRegisterBaseOffset = 0,
            loaderCode = stm32VlFlashLoader(),
        )
    }

    override fun supports(chipId: Int): Boolean = chipId == profile.chipId

    override fun readGeometry(
        programmer: Stm32StLinkProgrammer,
        targetInfo: Stm32SwdTargetInfo,
    ): Stm32StLinkFlashGeometry = programmer.loadFlashGeometry(profile)

    override fun flash(
        programmer: Stm32StLinkProgrammer,
        request: Stm32FlashRequest,
        targetInfo: Stm32SwdTargetInfo,
        geometry: Stm32StLinkFlashGeometry,
        progressListener: Stm32FlashProgressListener,
    ): Stm32StLinkFlashReport {
        require(request.startAddress % 2L == 0L) {
            "当前 SWD Flash 写入要求 startAddress 按 2 字节对齐"
        }
        val alignedFirmware = request.firmware.padToEvenBytesForSwd(request.padByte)
        programmer.reportProgress(
            progressListener = progressListener,
            stage = Stm32FlashStage.CONNECTING,
            overallPercent = 0.0,
            stageCompletedBytes = 0,
            stageTotalBytes = 0,
            message = "正在连接目标芯片",
        )
        programmer.validateFlashRange(
            geometry = geometry,
            startAddress = request.startAddress,
            length = alignedFirmware.size,
        )
        programmer.reportProgress(
            progressListener = progressListener,
            stage = Stm32FlashStage.CONNECTING,
            overallPercent = 10.0,
            stageCompletedBytes = 0,
            stageTotalBytes = 0,
            message = "已连接 ${targetInfo.probe.productName ?: "ST-Link"}，chipId=0x${targetInfo.chipId.toString(16).uppercase()}",
        )
        programmer.softResetAndHalt()
        when (request.eraseMode) {
            Stm32EraseMode.None -> {
                programmer.reportProgress(
                    progressListener = progressListener,
                    stage = Stm32FlashStage.ERASING,
                    overallPercent = 20.0,
                    stageCompletedBytes = 0,
                    stageTotalBytes = 0,
                    message = "跳过擦除",
                )
            }

            Stm32EraseMode.Mass -> {
                programmer.reportProgress(
                    progressListener = progressListener,
                    stage = Stm32FlashStage.ERASING,
                    overallPercent = 12.0,
                    stageCompletedBytes = 0,
                    stageTotalBytes = 0,
                    message = "正在整片擦除 Flash",
                )
                programmer.eraseFlashMass(profile)
                programmer.reportProgress(
                    progressListener = progressListener,
                    stage = Stm32FlashStage.ERASING,
                    overallPercent = 20.0,
                    stageCompletedBytes = 0,
                    stageTotalBytes = 0,
                    message = "整片擦除完成",
                )
            }

            is Stm32EraseMode.PageCodes -> {
                programmer.reportProgress(
                    progressListener = progressListener,
                    stage = Stm32FlashStage.ERASING,
                    overallPercent = 12.0,
                    stageCompletedBytes = 0,
                    stageTotalBytes = request.eraseMode.codes.size.toLong(),
                    message = "正在按页擦除 Flash",
                )
                request.eraseMode.codes.forEachIndexed { index, pageCode ->
                    programmer.eraseFlashPage(
                        profile = profile,
                        pageAddress = geometry.flashBaseAddress + (pageCode.toLong() * geometry.pageSizeBytes),
                    )
                    programmer.reportProgress(
                        progressListener = progressListener,
                        stage = Stm32FlashStage.ERASING,
                        overallPercent = 12.0 + ((index + 1).toDouble() / request.eraseMode.codes.size.toDouble() * 8.0),
                        stageCompletedBytes = (index + 1).toLong(),
                        stageTotalBytes = request.eraseMode.codes.size.toLong(),
                        message = "已擦除 ${index + 1}/${request.eraseMode.codes.size} 页",
                    )
                }
            }
        }
        programmer.programFlashWithLoader(
            profile = profile,
            geometry = geometry,
            startAddress = request.startAddress,
            firmware = alignedFirmware,
            progressListener = progressListener,
        )
        val verified =
            if (request.verifyAfterWrite) {
                programmer.verifyFlash(
                    startAddress = request.startAddress,
                    expected = alignedFirmware,
                    progressListener = progressListener,
                )
                true
            } else {
                programmer.reportProgress(
                    progressListener = progressListener,
                    stage = Stm32FlashStage.VERIFYING,
                    overallPercent = 90.0,
                    stageCompletedBytes = 0,
                    stageTotalBytes = 0,
                    message = "跳过回读校验",
                )
                false
            }
        val startedApplication =
            if (request.startApplicationAfterWrite) {
                programmer.reportProgress(
                    progressListener = progressListener,
                    stage = Stm32FlashStage.STARTING_APPLICATION,
                    overallPercent = 95.0,
                    stageCompletedBytes = 0,
                    stageTotalBytes = 0,
                    message = "正在复位并启动应用",
                )
                programmer.releaseDebugHalt()
                programmer.resetSystem()
                true
            } else {
                false
            }
        programmer.reportProgress(
            progressListener = progressListener,
            stage = Stm32FlashStage.COMPLETED,
            overallPercent = 100.0,
            stageCompletedBytes = alignedFirmware.size.toLong(),
            stageTotalBytes = alignedFirmware.size.toLong(),
            message = "SWD 烧录完成",
        )
        return Stm32StLinkFlashReport(
            targetInfo = targetInfo,
            geometry = geometry,
            bytesWritten = request.firmware.size,
            verified = verified,
            startedApplication = startedApplication,
        )
    }
}

private fun ByteArray.padToEvenBytesForSwd(padByte: Byte): ByteArray =
    if (size % 2 == 0) {
        this
    } else {
        copyOf(size + 1).also { output ->
            output[output.lastIndex] = padByte
        }
    }

private const val STM32_CHIPID_F1_HD = 0x414
private const val STM32_FLASH_BASE = 0x08000000L
private const val STM32_SRAM_BASE = 0x20000000L
private const val STM32F1_FLASH_KEYR = 0x40022004L
private const val STM32F1_FLASH_SR = 0x4002200CL
private const val STM32F1_FLASH_CR = 0x40022010L
private const val STM32F1_FLASH_AR = 0x40022014L
private const val STM32F1_FLASH_SIZE_REG = 0x1FFFF7E0L

private fun stm32VlFlashLoader(): ByteArray =
    byteArrayOf(
        0x00, 0xBF.toByte(), 0x00, 0xBF.toByte(),
        0x09, 0x4F, 0x1F, 0x44,
        0x09, 0x4D, 0x3D, 0x44,
        0x04, 0x88.toByte(), 0x0C, 0x80.toByte(),
        0x02, 0x30, 0x02, 0x31,
        0x4F, 0xF0.toByte(), 0x01, 0x07,
        0x2C, 0x68, 0x3C, 0x42,
        0xFC.toByte(), 0xD1.toByte(), 0x4F, 0xF0.toByte(),
        0x14, 0x07, 0x3C, 0x42,
        0x01, 0xD1.toByte(), 0x02, 0x3A,
        0xF0.toByte(), 0xDC.toByte(), 0x00, 0xBE.toByte(),
        0x00, 0x20, 0x02, 0x40,
        0x0C, 0x00, 0x00, 0x00,
    )

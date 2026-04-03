package site.addzero.stm32.bootloader

/**
 * ST-Link 发现与连接配置。
 */
data class StLinkConfig(
    /**
     * 可选的探针序列号。
     *
     * 当前这块板子的 ST-Link 没暴露序列号时，可以留空，
     * 库会在只有一块匹配设备时自动选中它。
     */
    val serialNumber: String? = null,
    /**
     * 进入 SWD 时是否执行 connect-under-reset。
     */
    val connectUnderReset: Boolean = true,
    /**
     * connect-under-reset 场景下，NRST 拉低保持时间。
     */
    val resetHoldTimeMs: Long = 20,
    /**
     * NRST 释放后，重复尝试强制 halt 的轮询次数。
     */
    val attachRetryCount: Int = 10,
    /**
     * attach 轮询间隔。
     */
    val attachRetryDelayMs: Long = 20,
) {
    init {
        require(resetHoldTimeMs >= 0) {
            "resetHoldTimeMs 不能小于 0"
        }
        require(attachRetryCount > 0) {
            "attachRetryCount 必须大于 0"
        }
        require(attachRetryDelayMs >= 0) {
            "attachRetryDelayMs 不能小于 0"
        }
    }
}

/**
 * 一个被发现的 ST-Link 设备。
 */
data class StLinkProbeInfo(
    val vendorId: Int,
    val productId: Int,
    val serialNumber: String?,
    val productName: String?,
    val manufacturerName: String?,
) {
    val productIdHex: String
        get() = "0x${productId.toString(16).uppercase().padStart(4, '0')}"
}

/**
 * ST-Link JTAG/SWD API 代际。
 */
enum class StLinkJtagApiVersion {
    V1,
    V2,
    V3,
}

/**
 * ST-Link 固件版本信息。
 */
data class StLinkVersion(
    val stlinkVersion: Int,
    val jtagVersion: Int,
    val swimVersion: Int,
    val vendorId: Int,
    val productId: Int,
    val jtagApiVersion: StLinkJtagApiVersion,
    val hasGetLastReadWriteStatus2: Boolean,
) {
    val versionText: String
        get() = "V$stlinkVersion J$jtagVersion M$swimVersion"
}

/**
 * 当前 ST-Link 处于什么模式。
 */
enum class StLinkCurrentMode(val rawValue: Int) {
    Dfu(0x00),
    MassStorage(0x01),
    Debug(0x02),
    Unknown(-1),
    ;

    companion object {
        fun fromRawValue(rawValue: Int): StLinkCurrentMode =
            entries.firstOrNull { mode -> mode.rawValue == rawValue } ?: Unknown
    }
}

/**
 * 一次 SWD attach 后拿到的目标信息。
 */
data class Stm32SwdTargetInfo(
    val probe: StLinkProbeInfo,
    val stLinkVersion: StLinkVersion,
    val currentMode: StLinkCurrentMode,
    val targetVoltageMillivolts: Int,
    /**
     * ST-Link `READ_IDCODES` 读到的 SW-DP/JTAG-DP ID。
     */
    val coreId: Long,
    /**
     * Cortex-M `CPUID` 原始值。
     */
    val cpuId: Long,
    /**
     * `DBGMCU_IDCODE` 低 12 位。
     */
    val chipId: Int,
    /**
     * 实际读取 `DBGMCU_IDCODE` 的寄存器地址。
     */
    val chipIdRegisterAddress: Long,
)

/**
 * 当前目标芯片可用的 Flash 几何信息。
 */
data class Stm32StLinkFlashGeometry(
    val flashBaseAddress: Long,
    val flashSizeBytes: Int,
    val pageSizeBytes: Int,
    val sramBaseAddress: Long,
    val sramSizeBytes: Int,
)

/**
 * 一次 ST-Link SWD 烧录结果。
 */
data class Stm32StLinkFlashReport(
    val targetInfo: Stm32SwdTargetInfo,
    val geometry: Stm32StLinkFlashGeometry,
    val bytesWritten: Int,
    val verified: Boolean,
    val startedApplication: Boolean,
)

/**
 * ST-Link 层异常。
 */
class StLinkException(
    message: String,
    cause: Throwable? = null,
) : Stm32BootloaderException(message, cause)

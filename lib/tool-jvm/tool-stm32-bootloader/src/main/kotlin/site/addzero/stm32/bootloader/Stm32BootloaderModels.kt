package site.addzero.stm32.bootloader

import site.addzero.serial.SerialParity
import site.addzero.serial.SerialPortConfig

/**
 * STM32 USART Bootloader 常用串口默认值。
 *
 * STM32 大多数系统 Bootloader 要求：
 * - 8 数据位
 * - 1 停止位
 * - 偶校验
 *
 * 但 STM32WB0 系列和 STM32WL3x 线按照官方说明使用无校验，
 * 因此这里保留 parity 可配。
 */
fun stm32BootloaderSerialConfig(
    portName: String,
    baudRate: Int = 115200,
    parity: SerialParity = SerialParity.EVEN,
    readTimeoutMs: Int = 1_000,
    writeTimeoutMs: Int = 1_000,
    openSafetySleepTimeMs: Int = 0,
): SerialPortConfig =
    SerialPortConfig(
        portName = portName,
        baudRate = baudRate,
        parity = parity,
        readTimeoutMs = readTimeoutMs,
        writeTimeoutMs = writeTimeoutMs,
        openSafetySleepTimeMs = openSafetySleepTimeMs,
    )

/**
 * STM32 USART Bootloader 主配置。
 */
data class Stm32BootloaderConfig(
    /**
     * 串口层配置。
     *
     * 建议优先通过 [stm32BootloaderSerialConfig] 创建，
     * 避免忘记把 parity 设成 STM32 Bootloader 需要的模式。
     */
    val serialConfig: SerialPortConfig,
    /**
     * 是否在 connect 时自动执行一次“进入系统 Bootloader”时序。
     */
    val autoEnterBootloaderOnConnect: Boolean = false,
    /**
     * 可选的 BOOT0 / NRST 线控参数。
     */
    val lineControl: Stm32BootloaderLineControl? = null,
    /**
     * connect 失败后的最大重试次数。
     *
     * 值为 1 表示只尝试一次，不额外重试。
     */
    val connectAttempts: Int = 3,
    /**
     * connect 重试间隔。
     */
    val connectRetryDelayMs: Long = 100,
    /**
     * 单次 Write Memory 的最大分块大小。
     *
     * STM32 USART Bootloader 最大允许 256 字节。
     */
    val writeChunkSize: Int = 256,
    /**
     * 单次 Read Memory 的最大分块大小。
     */
    val readChunkSize: Int = 256,
) {
    init {
        require(connectAttempts > 0) {
            "connectAttempts 必须大于 0"
        }
        require(connectRetryDelayMs >= 0) {
            "connectRetryDelayMs 不能小于 0"
        }
        require(writeChunkSize in 1..MAX_CHUNK_SIZE) {
            "writeChunkSize 必须在 1..$MAX_CHUNK_SIZE"
        }
        require(readChunkSize in 1..MAX_CHUNK_SIZE) {
            "readChunkSize 必须在 1..$MAX_CHUNK_SIZE"
        }
    }

    companion object {
        internal const val MAX_CHUNK_SIZE = 256
    }
}

/**
 * 进入/退出系统 Bootloader 时的串口控制线配置。
 *
 * 这里假设 BOOT0 / NRST 最终是通过 USB 转串口的 DTR / RTS 等线路控制。
 * 如果你的硬件有反相三极管或其他电平反转，使用 [Stm32ControlSignal.assertedOutput] 修正即可。
 */
data class Stm32BootloaderLineControl(
    /**
     * 逻辑 BOOT0 线。
     */
    val boot0: Stm32ControlSignal? = null,
    /**
     * 逻辑复位线。
     */
    val reset: Stm32ControlSignal? = null,
    /**
     * 在拉 BOOT0 之后等待多久再去拉复位，单位毫秒。
     */
    val boot0SetupDelayMs: Long = 20,
    /**
     * 复位脉宽，单位毫秒。
     */
    val resetPulseMs: Long = 40,
    /**
     * 复位释放后，给 ROM Bootloader 的启动等待时间。
     */
    val bootloaderReadyDelayMs: Long = 100,
    /**
     * BOOT0 拉回用户 Flash 后，给目标应用的启动等待时间。
     */
    val flashBootReadyDelayMs: Long = 100,
) {
    init {
        require(boot0SetupDelayMs >= 0) {
            "boot0SetupDelayMs 不能小于 0"
        }
        require(resetPulseMs >= 0) {
            "resetPulseMs 不能小于 0"
        }
        require(bootloaderReadyDelayMs >= 0) {
            "bootloaderReadyDelayMs 不能小于 0"
        }
        require(flashBootReadyDelayMs >= 0) {
            "flashBootReadyDelayMs 不能小于 0"
        }
    }
}

/**
 * 一根具体的宿主控制线与目标板逻辑信号的映射关系。
 */
data class Stm32ControlSignal(
    /**
     * 物理控制线。
     */
    val line: Stm32ControlLine,
    /**
     * 当逻辑信号被“assert”时，宿主输出端应该写入的布尔值。
     *
     * 例如：
     * - 直连场景通常是 `true`
     * - 经反相三极管后，往往需要填 `false`
     */
    val assertedOutput: Boolean = true,
)

/**
 * 串口可用控制线。
 */
enum class Stm32ControlLine {
    DTR,
    RTS,
}

/**
 * Bootloader 已知命令集合。
 */
enum class Stm32BootloaderCommand(val opcode: Int) {
    GET(0x00),
    GET_VERSION(0x01),
    GET_ID(0x02),
    READ_MEMORY(0x11),
    GO(0x21),
    WRITE_MEMORY(0x31),
    ERASE_MEMORY(0x43),
    EXTENDED_ERASE_MEMORY(0x44),
    WRITE_PROTECT(0x63),
    WRITE_UNPROTECT(0x73),
    READOUT_PROTECT(0x82),
    READOUT_UNPROTECT(0x92),
    GET_CHECKSUM(0xA1),
    ;

    companion object {
        fun fromOpcode(opcode: Int): Stm32BootloaderCommand? =
            entries.firstOrNull { command -> command.opcode == opcode }
    }
}

/**
 * 一次成功 connect 后获取到的 Bootloader 信息。
 */
data class Stm32BootloaderInfo(
    /**
     * `Get` 命令返回的协议版本号。
     *
     * 示例：`0x31` 表示 v3.1。
     */
    val protocolVersion: Int,
    /**
     * `Get Version` 命令返回的版本值。
     *
     * 多数情况下和 [protocolVersion] 一致；单独保留便于排查异常设备。
     */
    val bootloaderVersion: Int,
    /**
     * `Get ID` 返回的芯片 ID。
     */
    val chipId: Int,
    /**
     * `Get Version` 返回的两个兼容字段。
     *
     * 官方文档说明这两个字节保留给 legacy compatibility。
     */
    val optionByte1: Int,
    val optionByte2: Int,
    /**
     * Bootloader 声明支持的原始命令码。
     */
    val supportedCommandCodes: Set<Int>,
    /**
     * 能映射到已知命令枚举的子集。
     */
    val supportedCommands: Set<Stm32BootloaderCommand>,
) {
    /**
     * 便于日志打印的人类可读版本。
     */
    val protocolVersionText: String
        get() = decodeProtocolVersion(protocolVersion)
}

/**
 * 一次高层烧录请求。
 */
data class Stm32FlashRequest(
    /**
     * 固件起始写入地址。
     */
    val startAddress: Long = DEFAULT_FLASH_BASE_ADDRESS,
    /**
     * 待写入的固件字节流。
     */
    val firmware: ByteArray,
    /**
     * 擦除策略。
     */
    val eraseMode: Stm32EraseMode = Stm32EraseMode.Mass,
    /**
     * 写入完成后是否回读校验。
     */
    val verifyAfterWrite: Boolean = true,
    /**
     * 写入完成后是否启动应用。
     */
    val startApplicationAfterWrite: Boolean = true,
    /**
     * 尾块不足 4 字节时的补齐字节。
     *
     * STM32 USART `Write Memory` 要求 `N + 1` 必须是 4 的倍数。
     * 默认补 `0xFF`，这和擦除态 Flash 保持一致。
     */
    val padByte: Byte = 0xFF.toByte(),
) {
    init {
        require(startAddress in 0..MAX_U32_VALUE) {
            "startAddress 必须在 0..0xFFFFFFFF"
        }
        require(firmware.isNotEmpty()) {
            "firmware 不能为空"
        }
    }
}

/**
 * 擦除模式。
 *
 * 页码含义依芯片而定，调用方需要按 AN2606 / 具体 Reference Manual 传正确的页号或扇区号。
 */
sealed interface Stm32EraseMode {
    data object None : Stm32EraseMode

    data object Mass : Stm32EraseMode

    data class PageCodes(
        val codes: List<Int>,
    ) : Stm32EraseMode {
        init {
            require(codes.isNotEmpty()) {
                "codes 不能为空"
            }
            require(codes.all { code -> code in 0..0xFFFF }) {
                "页码必须在 0..0xFFFF"
            }
        }
    }
}

/**
 * 一次高层烧录结果。
 */
data class Stm32FlashReport(
    val bootloaderInfo: Stm32BootloaderInfo,
    val bytesWritten: Int,
    val eraseCommand: Stm32BootloaderCommand?,
    val verified: Boolean,
    val startedApplication: Boolean,
)

/**
 * 烧录阶段。
 *
 * 这个枚举用于上位机显示分阶段进度，而不是替代底层协议状态机。
 */
enum class Stm32FlashStage {
    ENTER_BOOTLOADER,
    CONNECTING,
    ERASING,
    WRITING,
    VERIFYING,
    STARTING_APPLICATION,
    COMPLETED,
}

/**
 * 一次烧录进度快照。
 */
data class Stm32FlashProgress(
    /**
     * 当前所在阶段。
     */
    val stage: Stm32FlashStage,
    /**
     * 当前阶段已完成的字节数。
     *
     * 对于非字节型阶段，例如擦除或启动，通常是 0 或 total。
     */
    val stageCompletedBytes: Long,
    /**
     * 当前阶段总字节数。
     *
     * 如果当前阶段不是按字节推进，这里会给 0。
     */
    val stageTotalBytes: Long,
    /**
     * 全流程百分比，范围 0.0..100.0。
     */
    val overallPercent: Double,
    /**
     * 当前阶段百分比，范围 0.0..100.0。
     */
    val stagePercent: Double,
    /**
     * 面向日志和 UI 的简短说明。
     */
    val message: String,
)

/**
 * 烧录进度监听器。
 */
fun interface Stm32FlashProgressListener {
    fun onProgress(progress: Stm32FlashProgress)
}

/**
 * 空进度监听器，避免业务代码自己传空 lambda。
 */
val NoopStm32FlashProgressListener = Stm32FlashProgressListener { }

/**
 * 协议层基础异常。
 */
open class Stm32BootloaderException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * 设备返回 NACK。
 */
class Stm32BootloaderNackException(
    message: String,
) : Stm32BootloaderException(message)

/**
 * 设备返回了不符合协议预期的内容。
 */
class Stm32BootloaderProtocolException(
    message: String,
) : Stm32BootloaderException(message)

internal const val DEFAULT_FLASH_BASE_ADDRESS: Long = 0x0800_0000
internal const val MAX_U32_VALUE: Long = 0xFFFF_FFFFL

private fun decodeProtocolVersion(version: Int): String {
    val major = (version shr 4) and 0x0F
    val minor = version and 0x0F
    return "v$major.$minor"
}

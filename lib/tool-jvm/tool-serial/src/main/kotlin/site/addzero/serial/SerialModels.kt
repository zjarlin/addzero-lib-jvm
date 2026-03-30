package site.addzero.serial

/**
 * 串口连接参数。
 *
 * 这组参数只描述“怎么打开一个串口”，不携带任何业务协议语义。
 * 例如 Modbus RTU、AT 指令、厂商私有二进制协议都可以复用这份配置。
 */
data class SerialPortConfig(
    /**
     * 串口名或设备路径。
     *
     * 示例：
     * - Windows: `COM3`
     * - Linux: `/dev/ttyUSB0`
     * - macOS: `/dev/cu.usbserial-1410`
     */
    val portName: String,
    /**
     * 波特率。
     *
     * 常见值有 `9600`、`19200`、`38400`、`57600`、`115200`。
     */
    val baudRate: Int = 9600,
    /**
     * 数据位。
     *
     * 常见场景通常使用 8。
     */
    val dataBits: Int = 8,
    /**
     * 停止位。
     */
    val stopBits: SerialStopBits = SerialStopBits.ONE,
    /**
     * 校验位。
     */
    val parity: SerialParity = SerialParity.NONE,
    /**
     * 流控模式。
     *
     * 大多数 USB 转串口场景直接用 `NONE` 即可。
     */
    val flowControl: SerialFlowControl = SerialFlowControl.NONE,
    /**
     * 单次读调用的超时时间，单位毫秒。
     *
     * 这里影响的是底层串口驱动的阻塞读取行为，
     * 不是业务协议里的整体请求超时。
     */
    val readTimeoutMs: Int = 1000,
    /**
     * 单次写调用的超时时间，单位毫秒。
     */
    val writeTimeoutMs: Int = 1000,
    /**
     * 打开串口后附加等待时长，单位毫秒。
     *
     * 某些设备在串口刚打开时会立即复位，
     * 这时可以留一点安全等待时间，再开始发送数据。
     */
    val openSafetySleepTimeMs: Int = 0,
) {
    init {
        require(portName.isNotBlank()) {
            "portName 不能为空"
        }
        require(baudRate > 0) {
            "baudRate 必须大于 0"
        }
        require(dataBits in 5..8) {
            "dataBits 只支持 5 到 8"
        }
        require(readTimeoutMs >= 0) {
            "readTimeoutMs 不能小于 0"
        }
        require(writeTimeoutMs >= 0) {
            "writeTimeoutMs 不能小于 0"
        }
        require(openSafetySleepTimeMs >= 0) {
            "openSafetySleepTimeMs 不能小于 0"
        }
    }
}

/**
 * 本机可见串口描述。
 *
 * 这些字段来自操作系统与设备驱动的枚举结果。
 * 某些字段在不同平台上可能为空，例如厂商名、位置信息或序列号。
 */
data class SerialPortDescriptor(
    /**
     * 系统级串口名，例如 `COM3`、`ttyUSB0`。
     */
    val systemPortName: String,
    /**
     * 系统级设备路径，例如 `/dev/ttyUSB0`。
     */
    val systemPortPath: String,
    /**
     * 人类可读的端口名。
     */
    val descriptivePortName: String,
    /**
     * 驱动提供的设备描述。
     */
    val portDescription: String,
    /**
     * 设备在宿主机上的物理位置信息。
     */
    val portLocation: String?,
    /**
     * 厂商名。
     */
    val manufacturer: String?,
    /**
     * 设备序列号。
     */
    val serialNumber: String?,
    /**
     * USB Vendor ID。
     */
    val vendorId: Int?,
    /**
     * USB Product ID。
     */
    val productId: Int?,
)

/**
 * 串口停止位配置。
 */
enum class SerialStopBits {
    /**
     * 1 位停止位。
     */
    ONE,
    /**
     * 1.5 位停止位。
     *
     * 这个选项比 `ONE` 和 `TWO` 少见，
     * 只有少部分旧设备或特定工业协议会要求。
     */
    ONE_POINT_FIVE,
    /**
     * 2 位停止位。
     */
    TWO,
}

/**
 * 串口校验位配置。
 */
enum class SerialParity {
    /**
     * 无校验。
     */
    NONE,
    /**
     * 偶校验。
     */
    EVEN,
    /**
     * 奇校验。
     */
    ODD,
    /**
     * MARK 校验。
     */
    MARK,
    /**
     * SPACE 校验。
     */
    SPACE,
}

/**
 * 串口流控配置。
 */
enum class SerialFlowControl {
    /**
     * 不启用流控。
     */
    NONE,
    /**
     * RTS/CTS 硬件流控。
     */
    RTS_CTS,
    /**
     * DTR/DSR 硬件流控。
     */
    DTR_DSR,
    /**
     * XON/XOFF 软件流控。
     */
    XON_XOFF,
}

/**
 * 串口基础异常。
 *
 * 用于包装打开、关闭、读写失败等底层错误。
 */
open class SerialPortException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * 串口读写超时异常。
 *
 * 这个异常只表示在预期时间内没有完成本次 IO，
 * 不代表设备一定断开，也不代表串口参数一定配置错误。
 */
class SerialTimeoutException(
    message: String,
    cause: Throwable? = null,
) : SerialPortException(message, cause)

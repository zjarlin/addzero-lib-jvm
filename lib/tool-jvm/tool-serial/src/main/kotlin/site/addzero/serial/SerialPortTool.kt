package site.addzero.serial

import com.fazecast.jSerialComm.SerialPort

/**
 * 串口工具入口。
 *
 * 这个对象只负责两件事：
 * 1. 枚举当前宿主机已经暴露出来的串口
 * 2. 按 [SerialPortConfig] 打开一个可管理生命周期的 [SerialConnection]
 *
 * 底层实现使用 `jSerialComm`，因此：
 * - Windows 常见端口名形如 `COM3`
 * - Linux 常见端口名形如 `/dev/ttyUSB0`
 * - macOS 常见端口名形如 `/dev/cu.usbserial-xxxx`
 */
object SerialPortTool {
    /**
     * 列出当前机器可见的串口。
     *
     * 返回值来自操作系统当前已经识别的设备列表；
     * 这一步不会真正占用串口，也不会尝试建立连接。
     */
    fun listPorts(): List<SerialPortDescriptor> =
        SerialPort.getCommPorts()
            .map { port -> port.toDescriptor() }
            .sortedBy { descriptor -> descriptor.systemPortName }

    /**
     * 打开一个串口连接。
     *
     * 调用成功后，底层串口已经被占用，必须在使用结束后调用 [SerialConnection.close]，
     * 或者直接使用 [use] 让 Kotlin 自动关闭。
     */
    fun open(config: SerialPortConfig): SerialConnection =
        SerialConnection(
            driver = JSerialCommDriver(SerialPort.getCommPort(config.portName), config),
            config = config,
        )

    /**
     * 按 Kotlin `use` 风格包一层，避免调用方忘记关闭串口。
     *
     * 适合“一次打开、一次请求、立即关闭”的场景，例如：
     * - 发一条 AT 指令
     * - 读取一次设备版本号
     * - 做一次串口连通性自检
     */
    inline fun <T> use(
        config: SerialPortConfig,
        block: (SerialConnection) -> T,
    ): T = open(config).use(block)
}

private fun SerialPort.toDescriptor(): SerialPortDescriptor =
    SerialPortDescriptor(
        /**
         * `jSerialComm` 某些平台下可能给空字符串或 null，
         * 这里统一兜底成稳定的 Kotlin 非空字段。
         */
        systemPortName = systemPortName.orEmpty(),
        systemPortPath = systemPortPath.orEmpty(),
        descriptivePortName = descriptivePortName.orEmpty(),
        portDescription = portDescription.orEmpty(),
        portLocation = portLocation?.takeIf { it.isNotBlank() },
        manufacturer = manufacturer?.takeIf { it.isNotBlank() },
        serialNumber = serialNumber?.takeIf { it.isNotBlank() },
        vendorId = vendorID.takeIf { it > 0 },
        productId = productID.takeIf { it > 0 },
    )

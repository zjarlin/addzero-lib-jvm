package site.addzero.device.driver.modbus.rtu

/**
 * 单个 Modbus RTU 终端的默认连接参数。
 *
 * @property serviceId 运行时内部使用的服务标识，通常由 KSP 生成网关引用
 * @property portPath 串口设备路径，例如 `/dev/ttyUSB0`
 * @property unitId Modbus 从站地址
 * @property baudRate 串口波特率
 * @property dataBits 数据位，默认 8 位
 * @property stopBits 停止位，目前按 1 或 2 位映射到底层串口参数
 * @property parity 串口奇偶校验位
 * @property timeoutMs 单次请求超时时间，单位毫秒
 * @property retries 失败后的额外重试次数，不包含首次请求
 */
data class ModbusRtuEndpointConfig(
    val serviceId: String,
    val portPath: String,
    val unitId: Int,
    val baudRate: Int,
    val dataBits: Int = 8,
    val stopBits: Int = 1,
    val parity: ModbusSerialParity = ModbusSerialParity.NONE,
    val timeoutMs: Long,
    val retries: Int,
)

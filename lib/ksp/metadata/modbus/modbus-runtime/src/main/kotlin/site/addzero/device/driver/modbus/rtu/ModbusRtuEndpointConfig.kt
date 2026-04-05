package site.addzero.device.driver.modbus.rtu

/**
 * Modbus RTU 终端连接参数抽象。
 *
 * 业务应用通常只需要向 Koin 提供一个全局默认实现；
 * 生成网关和 Spring 路由源码会基于这份默认值再叠加请求级覆盖项。
 */
interface ModbusRtuEndpointConfig {
    val portPath: String
    val unitId: Int
    val baudRate: Int
    val dataBits: Int
    val stopBits: Int
    val parity: ModbusSerialParity
    val timeoutMs: Long
    val retries: Int
}

/**
 * 默认的不可变 RTU 配置实现。
 */
data class DefaultModbusRtuEndpointConfig(
    override val portPath: String,
    override val unitId: Int,
    override val baudRate: Int,
    override val dataBits: Int = 8,
    override val stopBits: Int = 1,
    override val parity: ModbusSerialParity = ModbusSerialParity.NONE,
    override val timeoutMs: Long,
    override val retries: Int,
) : ModbusRtuEndpointConfig

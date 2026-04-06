package site.addzero.device.driver.modbus.rtu

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

package site.addzero.modbus.rtu.client

import site.addzero.serial.SerialPortConfig

/**
 * Modbus RTU 客户端配置。
 *
 * 串口本身的参数全部复用 [SerialPortConfig]，
 * 这里额外补充的只是 Modbus 协议层需要的内容。
 */
data class ModbusRtuClientConfig(
    val serialConfig: SerialPortConfig,
    val unitId: Int = 1,
    val requestTimeoutMs: Int = serialConfig.readTimeoutMs,
    val retries: Int = 0,
    val reconnectPerRequest: Boolean = false,
    val transmitDelayMs: Int = -1,
) {
    init {
        require(unitId in 0..255) {
            "unitId 必须在 0..255"
        }
        require(requestTimeoutMs >= 0) {
            "requestTimeoutMs 不能小于 0"
        }
        require(retries >= 0) {
            "retries 不能小于 0"
        }
    }
}

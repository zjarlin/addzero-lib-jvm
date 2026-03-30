package site.addzero.modbus.rtu.server

import site.addzero.serial.SerialPortConfig

/**
 * Modbus RTU 服务端配置。
 */
data class ModbusRtuServerConfig(
    val serialConfig: SerialPortConfig,
    val defaultUnitId: Int = 1,
) {
    init {
        require(defaultUnitId in 0..255) {
            "defaultUnitId 必须在 0..255"
        }
    }
}

package site.addzero.modbus.rtu.server

import site.addzero.serial.SerialPortConfig

/**
 * Modbus RTU 服务端配置。
 */
data class ModbusRtuServerConfig(
    /**
     * 串口监听参数。
     */
    val serialConfig: SerialPortConfig,
    /**
     * 默认自动创建的从站地址。
     */
    val defaultUnitId: Int = 1,
) {
    init {
        require(defaultUnitId in 0..255) {
            "defaultUnitId 必须在 0..255"
        }
    }
}

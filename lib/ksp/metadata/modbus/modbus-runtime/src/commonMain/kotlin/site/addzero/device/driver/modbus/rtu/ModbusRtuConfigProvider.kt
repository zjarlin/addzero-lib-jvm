package site.addzero.device.driver.modbus.rtu

interface ModbusRtuConfigProvider {
    fun defaultConfig(): ModbusRtuEndpointConfig
}

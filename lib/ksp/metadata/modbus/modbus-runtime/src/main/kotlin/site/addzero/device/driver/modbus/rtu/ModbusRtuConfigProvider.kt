package site.addzero.device.driver.modbus.rtu

/**
 * 提供某个 RTU 服务默认配置的注册点。
 *
 * 一般由 KSP 生成代码或业务模块实现，再交给 [ModbusRtuConfigRegistry] 汇总。
 */
interface ModbusRtuConfigProvider {
    val serviceId: String

    fun defaultConfig(): ModbusRtuEndpointConfig
}

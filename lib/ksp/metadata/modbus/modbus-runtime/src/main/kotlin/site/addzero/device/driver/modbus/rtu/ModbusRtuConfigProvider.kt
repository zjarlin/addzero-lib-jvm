package site.addzero.device.driver.modbus.rtu

/**
 * 提供全局 RTU 默认配置的注册点。
 *
 * 运行时只保留一份默认配置；
 * 生成网关或 Spring 路由再根据请求内容做局部覆盖。
 */
interface ModbusRtuConfigProvider {
    fun defaultConfig(): ModbusRtuEndpointConfig
}

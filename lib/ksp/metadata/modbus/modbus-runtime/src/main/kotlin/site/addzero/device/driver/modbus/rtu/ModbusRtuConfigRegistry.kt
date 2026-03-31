package site.addzero.device.driver.modbus.rtu

/**
 * 按 `serviceId` 汇总默认 RTU 配置的查询表。
 *
 * 运行时只做快速查找，不承担动态刷新、热更新或多层配置合并职责。
 */
class ModbusRtuConfigRegistry(
    providers: List<ModbusRtuConfigProvider>,
) {
    private val configs: Map<String, ModbusRtuEndpointConfig> =
        providers.associate { provider -> provider.serviceId to provider.defaultConfig() }

    fun require(serviceId: String): ModbusRtuEndpointConfig =
        configs[serviceId] ?: error("未找到 Modbus RTU 配置：$serviceId")
}

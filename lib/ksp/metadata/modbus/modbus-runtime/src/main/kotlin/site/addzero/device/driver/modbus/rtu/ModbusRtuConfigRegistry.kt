package site.addzero.device.driver.modbus.rtu

/**
 * 单默认值 RTU 配置的兼容包装器。
 *
 * 旧版本会按 `serviceId` 聚合多份配置；
 * 现在运行时只接受一份全局默认配置，这里保留兼容入口，避免外部装配点立刻失效。
 */
class ModbusRtuConfigRegistry(
    private val provider: ModbusRtuConfigProvider,
) {
    constructor(providers: List<ModbusRtuConfigProvider>) : this(
        provider =
            providers.singleOrNull()
                ?: error("Modbus RTU 运行时只允许一份全局默认配置，请只注册一个 ModbusRtuConfigProvider。"),
    )

    fun defaultConfig(): ModbusRtuEndpointConfig = provider.defaultConfig()
}

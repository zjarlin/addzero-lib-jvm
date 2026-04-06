package site.addzero.device.driver.modbus.rtu

class ModbusRtuConfigRegistry(
    private val provider: ModbusRtuConfigProvider,
) {
    constructor(providers: List<ModbusRtuConfigProvider>) : this(
        provider =
            providers.singleOrNull()
                ?: error("Modbus RTU runtime allows exactly one default config provider."),
    )

    fun defaultConfig(): ModbusRtuEndpointConfig = provider.defaultConfig()
}

package site.addzero.device.driver.modbus.mqtt

data class ModbusMqttEndpointConfig(
    val serviceId: String,
    val brokerUrl: String,
    val clientId: String,
    val requestTopic: String,
    val responseTopic: String,
    val qos: Int,
    val timeoutMs: Long,
    val retries: Int,
)

interface ModbusMqttConfigProvider {
    val serviceId: String

    fun defaultConfig(): ModbusMqttEndpointConfig
}

class ModbusMqttConfigRegistry(
    providers: List<ModbusMqttConfigProvider>,
) {
    private val configs =
        providers.associate { provider -> provider.serviceId to provider.defaultConfig() }

    fun require(serviceId: String): ModbusMqttEndpointConfig =
        configs[serviceId] ?: error("Missing Modbus MQTT config: $serviceId")
}

interface ModbusMqttExecutor {
    suspend fun readCoils(
        config: ModbusMqttEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int>

    suspend fun readDiscreteInputs(
        config: ModbusMqttEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int>

    suspend fun readHoldingRegisters(
        config: ModbusMqttEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int>

    suspend fun readInputRegisters(
        config: ModbusMqttEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int>

    suspend fun writeSingleCoil(
        config: ModbusMqttEndpointConfig,
        address: Int,
        value: Boolean,
    )

    suspend fun writeMultipleCoils(
        config: ModbusMqttEndpointConfig,
        address: Int,
        values: List<Boolean>,
    )

    suspend fun writeSingleRegister(
        config: ModbusMqttEndpointConfig,
        address: Int,
        value: Int,
    )

    suspend fun writeMultipleRegisters(
        config: ModbusMqttEndpointConfig,
        address: Int,
        values: List<Int>,
    )
}

open class UnsupportedModbusMqttExecutor : ModbusMqttExecutor {
    override suspend fun readCoils(config: ModbusMqttEndpointConfig, address: Int, quantity: Int): List<Int> =
        unsupported("readCoils", config)

    override suspend fun readDiscreteInputs(config: ModbusMqttEndpointConfig, address: Int, quantity: Int): List<Int> =
        unsupported("readDiscreteInputs", config)

    override suspend fun readHoldingRegisters(config: ModbusMqttEndpointConfig, address: Int, quantity: Int): List<Int> =
        unsupported("readHoldingRegisters", config)

    override suspend fun readInputRegisters(config: ModbusMqttEndpointConfig, address: Int, quantity: Int): List<Int> =
        unsupported("readInputRegisters", config)

    override suspend fun writeSingleCoil(config: ModbusMqttEndpointConfig, address: Int, value: Boolean) {
        unsupported<Unit>("writeSingleCoil", config)
    }

    override suspend fun writeMultipleCoils(config: ModbusMqttEndpointConfig, address: Int, values: List<Boolean>) {
        unsupported<Unit>("writeMultipleCoils", config)
    }

    override suspend fun writeSingleRegister(config: ModbusMqttEndpointConfig, address: Int, value: Int) {
        unsupported<Unit>("writeSingleRegister", config)
    }

    override suspend fun writeMultipleRegisters(config: ModbusMqttEndpointConfig, address: Int, values: List<Int>) {
        unsupported<Unit>("writeMultipleRegisters", config)
    }

    protected fun <T> unsupported(
        operation: String,
        config: ModbusMqttEndpointConfig,
    ): T {
        error(
            "Modbus MQTT 执行器尚未绑定真实 broker 适配实现：operation=$operation service=${config.serviceId} broker=${config.brokerUrl}",
        )
    }
}

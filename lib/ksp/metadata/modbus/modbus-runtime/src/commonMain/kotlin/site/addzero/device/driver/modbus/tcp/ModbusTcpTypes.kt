package site.addzero.device.driver.modbus.tcp

data class ModbusTcpEndpointConfig(
    val serviceId: String,
    val host: String,
    val port: Int,
    val unitId: Int,
    val timeoutMs: Long,
    val retries: Int,
)

interface ModbusTcpConfigProvider {
    val serviceId: String
    fun defaultConfig(): ModbusTcpEndpointConfig
}

class ModbusTcpConfigRegistry(
    providers: List<ModbusTcpConfigProvider>,
) {
    private val configs =
        providers.associate { provider -> provider.serviceId to provider.defaultConfig() }

    fun require(serviceId: String): ModbusTcpEndpointConfig =
        configs[serviceId] ?: error("Missing Modbus TCP config: $serviceId")
}

interface ModbusTcpExecutor {
    suspend fun readCoils(
        config: ModbusTcpEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int>

    suspend fun readDiscreteInputs(
        config: ModbusTcpEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int>

    suspend fun readHoldingRegisters(
        config: ModbusTcpEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int>

    suspend fun readInputRegisters(
        config: ModbusTcpEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int>

    suspend fun writeSingleCoil(
        config: ModbusTcpEndpointConfig,
        address: Int,
        value: Boolean,
    )

    suspend fun writeMultipleCoils(
        config: ModbusTcpEndpointConfig,
        address: Int,
        values: List<Boolean>,
    )

    suspend fun writeSingleRegister(
        config: ModbusTcpEndpointConfig,
        address: Int,
        value: Int,
    )

    suspend fun writeMultipleRegisters(
        config: ModbusTcpEndpointConfig,
        address: Int,
        values: List<Int>,
    )
}

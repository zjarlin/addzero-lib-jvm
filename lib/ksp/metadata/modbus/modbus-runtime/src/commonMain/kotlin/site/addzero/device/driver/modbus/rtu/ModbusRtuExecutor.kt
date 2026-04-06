package site.addzero.device.driver.modbus.rtu

interface ModbusRtuExecutor {
    suspend fun readCoils(
        config: ModbusRtuEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int>

    suspend fun readDiscreteInputs(
        config: ModbusRtuEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int>

    suspend fun readHoldingRegisters(
        config: ModbusRtuEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int>

    suspend fun readInputRegisters(
        config: ModbusRtuEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int>

    suspend fun writeSingleCoil(
        config: ModbusRtuEndpointConfig,
        address: Int,
        value: Boolean,
    )

    suspend fun writeMultipleCoils(
        config: ModbusRtuEndpointConfig,
        address: Int,
        values: List<Boolean>,
    )

    suspend fun writeSingleRegister(
        config: ModbusRtuEndpointConfig,
        address: Int,
        value: Int,
    )

    suspend fun writeMultipleRegisters(
        config: ModbusRtuEndpointConfig,
        address: Int,
        values: List<Int>,
    )
}

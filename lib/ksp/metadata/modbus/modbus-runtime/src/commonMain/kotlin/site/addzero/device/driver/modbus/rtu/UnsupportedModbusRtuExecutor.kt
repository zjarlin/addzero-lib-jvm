package site.addzero.device.driver.modbus.rtu

class UnsupportedModbusRtuExecutor : ModbusRtuExecutor {
    override suspend fun readCoils(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        error("Default RTU executor is not wired: port=${config.portPath} unit=${config.unitId}")

    override suspend fun readDiscreteInputs(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        error("Default RTU executor is not wired: port=${config.portPath} unit=${config.unitId}")

    override suspend fun readHoldingRegisters(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        error("Default RTU executor is not wired: port=${config.portPath} unit=${config.unitId}")

    override suspend fun readInputRegisters(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        error("Default RTU executor is not wired: port=${config.portPath} unit=${config.unitId}")

    override suspend fun writeSingleCoil(config: ModbusRtuEndpointConfig, address: Int, value: Boolean) {
        error("Default RTU executor is not wired: port=${config.portPath} unit=${config.unitId}")
    }

    override suspend fun writeMultipleCoils(config: ModbusRtuEndpointConfig, address: Int, values: List<Boolean>) {
        error("Default RTU executor is not wired: port=${config.portPath} unit=${config.unitId}")
    }

    override suspend fun writeSingleRegister(config: ModbusRtuEndpointConfig, address: Int, value: Int) {
        error("Default RTU executor is not wired: port=${config.portPath} unit=${config.unitId}")
    }

    override suspend fun writeMultipleRegisters(config: ModbusRtuEndpointConfig, address: Int, values: List<Int>) {
        error("Default RTU executor is not wired: port=${config.portPath} unit=${config.unitId}")
    }
}

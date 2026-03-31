package site.addzero.device.driver.modbus.rtu

/**
 * 默认占位执行器。
 *
 * 当宿主应用还没有接入真实通信能力时，明确在调用点失败，避免静默降级。
 */
class UnsupportedModbusRtuExecutor : ModbusRtuExecutor {
    override suspend fun readCoils(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        error("默认 RTU 执行器尚未接入真实通信实现：${config.serviceId}")

    override suspend fun readDiscreteInputs(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        error("默认 RTU 执行器尚未接入真实通信实现：${config.serviceId}")

    override suspend fun readHoldingRegisters(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        error("默认 RTU 执行器尚未接入真实通信实现：${config.serviceId}")

    override suspend fun readInputRegisters(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        error("默认 RTU 执行器尚未接入真实通信实现：${config.serviceId}")

    override suspend fun writeSingleCoil(config: ModbusRtuEndpointConfig, address: Int, value: Boolean) {
        error("默认 RTU 执行器尚未接入真实通信实现：${config.serviceId}")
    }

    override suspend fun writeMultipleCoils(config: ModbusRtuEndpointConfig, address: Int, values: List<Boolean>) {
        error("默认 RTU 执行器尚未接入真实通信实现：${config.serviceId}")
    }

    override suspend fun writeSingleRegister(config: ModbusRtuEndpointConfig, address: Int, value: Int) {
        error("默认 RTU 执行器尚未接入真实通信实现：${config.serviceId}")
    }

    override suspend fun writeMultipleRegisters(config: ModbusRtuEndpointConfig, address: Int, values: List<Int>) {
        error("默认 RTU 执行器尚未接入真实通信实现：${config.serviceId}")
    }
}

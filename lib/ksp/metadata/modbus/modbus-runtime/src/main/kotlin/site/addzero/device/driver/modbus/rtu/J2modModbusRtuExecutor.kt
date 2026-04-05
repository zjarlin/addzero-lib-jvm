package site.addzero.device.driver.modbus.rtu

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import site.addzero.modbus.rtu.client.ModbusRtuClient
import site.addzero.modbus.rtu.client.ModbusRtuClientConfig
import site.addzero.serial.SerialParity
import site.addzero.serial.SerialPortConfig
import site.addzero.serial.SerialStopBits

/**
 * 兼容旧类名的 RTU 执行器。
 *
 * 这里仍保留 `J2mod` 命名，避免影响外部依赖和 KSP 生成产物；
 * 真实读写已经委托给 `tool-modbus` / `tool-serial`。
 */
@Single
class J2modModbusRtuExecutor internal constructor(
    private val clientFactory: (ModbusRtuEndpointConfig) -> ToolModbusRtuClient = ::defaultToolModbusRtuClient,
) : ModbusRtuExecutor {
    override suspend fun readCoils(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        runRtu(config) { client ->
            client.readCoils(address, quantity).map { bit -> if (bit) 1 else 0 }
        }

    override suspend fun readDiscreteInputs(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        runRtu(config) { client ->
            client.readDiscreteInputs(address, quantity).map { bit -> if (bit) 1 else 0 }
        }

    override suspend fun readHoldingRegisters(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        runRtu(config) { client ->
            client.readHoldingRegisters(address, quantity)
        }

    override suspend fun readInputRegisters(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        runRtu(config) { client ->
            client.readInputRegisters(address, quantity)
        }

    override suspend fun writeSingleCoil(config: ModbusRtuEndpointConfig, address: Int, value: Boolean) {
        runRtu(config) { client ->
            client.writeSingleCoil(address, value)
        }
    }

    override suspend fun writeMultipleCoils(config: ModbusRtuEndpointConfig, address: Int, values: List<Boolean>) {
        runRtu(config) { client ->
            client.writeMultipleCoils(address, values)
        }
    }

    override suspend fun writeSingleRegister(config: ModbusRtuEndpointConfig, address: Int, value: Int) {
        runRtu(config) { client ->
            client.writeSingleRegister(address, value)
        }
    }

    override suspend fun writeMultipleRegisters(config: ModbusRtuEndpointConfig, address: Int, values: List<Int>) {
        runRtu(config) { client ->
            client.writeMultipleRegisters(address, values)
        }
    }

    private suspend fun <T> runRtu(
        config: ModbusRtuEndpointConfig,
        block: (ToolModbusRtuClient) -> T,
    ): T = withContext(Dispatchers.IO) {
        val client = clientFactory(config)
        try {
            block(client)
        } catch (throwable: Throwable) {
            throw IllegalStateException(
                "Modbus RTU 通信失败：port=${config.portPath} unit=${config.unitId}",
                throwable,
            )
        } finally {
            runCatching { client.close() }
        }
    }
}

internal interface ToolModbusRtuClient : AutoCloseable {
    fun readCoils(address: Int, quantity: Int): List<Boolean>

    fun readDiscreteInputs(address: Int, quantity: Int): List<Boolean>

    fun readHoldingRegisters(address: Int, quantity: Int): List<Int>

    fun readInputRegisters(address: Int, quantity: Int): List<Int>

    fun writeSingleCoil(address: Int, value: Boolean)

    fun writeMultipleCoils(address: Int, values: List<Boolean>)

    fun writeSingleRegister(address: Int, value: Int)

    fun writeMultipleRegisters(address: Int, values: List<Int>)

    override fun close()
}

internal fun ModbusRtuEndpointConfig.toClientConfig(): ModbusRtuClientConfig {
    return ModbusRtuClientConfig(
        serialConfig = SerialPortConfig(
            portName = portPath,
            baudRate = baudRate,
            dataBits = dataBits,
            stopBits = stopBits.toSerialStopBits(),
            parity = parity.toSerialParity(),
            readTimeoutMs = timeoutMs.toInt(),
            writeTimeoutMs = timeoutMs.toInt(),
        ),
        unitId = unitId,
        requestTimeoutMs = timeoutMs.toInt(),
        retries = retries.coerceAtLeast(0),
        reconnectPerRequest = true,
    )
}

private fun defaultToolModbusRtuClient(
    config: ModbusRtuEndpointConfig,
): ToolModbusRtuClient {
    return ToolModbusRtuClientAdapter(ModbusRtuClient(config.toClientConfig()))
}

private fun Int.toSerialStopBits(): SerialStopBits =
    when (this) {
        2 -> SerialStopBits.TWO
        else -> SerialStopBits.ONE
    }

private fun ModbusSerialParity.toSerialParity(): SerialParity =
    when (this) {
        ModbusSerialParity.NONE -> SerialParity.NONE
        ModbusSerialParity.EVEN -> SerialParity.EVEN
        ModbusSerialParity.ODD -> SerialParity.ODD
    }

private class ToolModbusRtuClientAdapter(
    private val delegate: ModbusRtuClient,
) : ToolModbusRtuClient {
    override fun readCoils(address: Int, quantity: Int): List<Boolean> = delegate.readCoils(address, quantity)

    override fun readDiscreteInputs(address: Int, quantity: Int): List<Boolean> = delegate.readDiscreteInputs(address, quantity)

    override fun readHoldingRegisters(address: Int, quantity: Int): List<Int> = delegate.readHoldingRegisters(address, quantity)

    override fun readInputRegisters(address: Int, quantity: Int): List<Int> = delegate.readInputRegisters(address, quantity)

    override fun writeSingleCoil(address: Int, value: Boolean) {
        delegate.writeSingleCoil(address, value)
    }

    override fun writeMultipleCoils(address: Int, values: List<Boolean>) {
        delegate.writeMultipleCoils(address, values)
    }

    override fun writeSingleRegister(address: Int, value: Int) {
        delegate.writeSingleRegister(address, value)
    }

    override fun writeMultipleRegisters(address: Int, values: List<Int>) {
        delegate.writeMultipleRegisters(address, values)
    }

    override fun close() {
        delegate.close()
    }
}

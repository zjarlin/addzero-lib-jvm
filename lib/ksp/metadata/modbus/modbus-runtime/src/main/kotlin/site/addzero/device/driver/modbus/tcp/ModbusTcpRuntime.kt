package site.addzero.device.driver.modbus.tcp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.modbus.tcp.client.ModbusTcpClient
import site.addzero.modbus.tcp.client.ModbusTcpClientConfig

@Single
class J2modModbusTcpExecutor internal constructor(
    private val clientFactory: (ModbusTcpEndpointConfig) -> ToolModbusTcpClient = ::defaultToolModbusTcpClient,
) : ModbusTcpExecutor {
    override suspend fun readCoils(config: ModbusTcpEndpointConfig, address: Int, quantity: Int): List<Int> =
        runTcp(config) { client ->
            client.readCoils(address, quantity).map { bit -> if (bit) 1 else 0 }
        }

    override suspend fun readDiscreteInputs(config: ModbusTcpEndpointConfig, address: Int, quantity: Int): List<Int> =
        runTcp(config) { client ->
            client.readDiscreteInputs(address, quantity).map { bit -> if (bit) 1 else 0 }
        }

    override suspend fun readHoldingRegisters(config: ModbusTcpEndpointConfig, address: Int, quantity: Int): List<Int> =
        runTcp(config) { client ->
            client.readHoldingRegisters(address, quantity)
        }

    override suspend fun readInputRegisters(config: ModbusTcpEndpointConfig, address: Int, quantity: Int): List<Int> =
        runTcp(config) { client ->
            client.readInputRegisters(address, quantity)
        }

    override suspend fun writeSingleCoil(config: ModbusTcpEndpointConfig, address: Int, value: Boolean) {
        runTcp(config) { client ->
            client.writeSingleCoil(address, value)
        }
    }

    override suspend fun writeMultipleCoils(config: ModbusTcpEndpointConfig, address: Int, values: List<Boolean>) {
        runTcp(config) { client ->
            client.writeMultipleCoils(address, values)
        }
    }

    override suspend fun writeSingleRegister(config: ModbusTcpEndpointConfig, address: Int, value: Int) {
        runTcp(config) { client ->
            client.writeSingleRegister(address, value)
        }
    }

    override suspend fun writeMultipleRegisters(config: ModbusTcpEndpointConfig, address: Int, values: List<Int>) {
        runTcp(config) { client ->
            client.writeMultipleRegisters(address, values)
        }
    }

    private suspend fun <T> runTcp(
        config: ModbusTcpEndpointConfig,
        block: (ToolModbusTcpClient) -> T,
    ): T = withContext(Dispatchers.IO) {
        val client = clientFactory(config)
        try {
            block(client)
        } catch (throwable: Throwable) {
            throw IllegalStateException(
                "Modbus TCP 通信失败：service=${config.serviceId} host=${config.host}:${config.port} unit=${config.unitId}",
                throwable,
            )
        } finally {
            runCatching { client.close() }
        }
    }
}

/**
 * Modbus TCP 运行时的 Koin 模块。
 *
 * 这里只扫描执行器实现；配置注册表由上层应用按具体 provider 显式组装。
 */
@Module
@ComponentScan("site.addzero.device.driver.modbus.tcp")
class ModbusTcpRuntimeKoinModule

internal interface ToolModbusTcpClient : AutoCloseable {
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

internal fun ModbusTcpEndpointConfig.toClientConfig(): ModbusTcpClientConfig {
    return ModbusTcpClientConfig(
        host = host,
        port = port,
        unitId = unitId,
        timeoutMs = timeoutMs.toInt(),
        reconnectPerRequest = true,
    )
}

private fun defaultToolModbusTcpClient(
    config: ModbusTcpEndpointConfig,
): ToolModbusTcpClient {
    return ToolModbusTcpClientAdapter(ModbusTcpClient(config.toClientConfig()))
}

private class ToolModbusTcpClientAdapter(
    private val delegate: ModbusTcpClient,
) : ToolModbusTcpClient {
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

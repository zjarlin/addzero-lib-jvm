package site.addzero.device.driver.modbus.tcp

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster
import com.ghgande.j2mod.modbus.procimg.SimpleRegister
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

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
    private val configs: Map<String, ModbusTcpEndpointConfig> =
        providers.associate { provider -> provider.serviceId to provider.defaultConfig() }

    fun require(serviceId: String): ModbusTcpEndpointConfig =
        configs[serviceId] ?: error("未找到 Modbus TCP 配置：$serviceId")
}

interface ModbusTcpExecutor {
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

@Single
class J2modModbusTcpExecutor : ModbusTcpExecutor {
    override suspend fun readHoldingRegisters(config: ModbusTcpEndpointConfig, address: Int, quantity: Int): List<Int> =
        withTcpMaster(config) { master ->
            master.readMultipleRegisters(config.unitId, address, quantity).map { register -> register.toUnsignedShort() }
        }

    override suspend fun readInputRegisters(config: ModbusTcpEndpointConfig, address: Int, quantity: Int): List<Int> =
        withTcpMaster(config) { master ->
            master.readInputRegisters(config.unitId, address, quantity).map { register -> register.toUnsignedShort() }
        }

    override suspend fun writeSingleCoil(config: ModbusTcpEndpointConfig, address: Int, value: Boolean) {
        withTcpMaster(config) { master ->
            master.writeCoil(config.unitId, address, value)
        }
    }

    override suspend fun writeSingleRegister(config: ModbusTcpEndpointConfig, address: Int, value: Int) {
        withTcpMaster(config) { master ->
            master.writeSingleRegister(config.unitId, address, SimpleRegister(value and 0xFFFF))
        }
    }

    override suspend fun writeMultipleRegisters(config: ModbusTcpEndpointConfig, address: Int, values: List<Int>) {
        withTcpMaster(config) { master ->
            val registers = values.map { value -> SimpleRegister(value and 0xFFFF) }.toTypedArray()
            master.writeMultipleRegisters(config.unitId, address, registers)
        }
    }

    private suspend fun <T> withTcpMaster(
        config: ModbusTcpEndpointConfig,
        block: suspend (ModbusTCPMaster) -> T,
    ): T = withContext(Dispatchers.IO) {
        val totalAttempts = config.retries.coerceAtLeast(0) + 1
        var lastError: Throwable? = null
        repeat(totalAttempts) { attempt ->
            val master = ModbusTCPMaster(config.host, config.port)
            master.setTimeout(config.timeoutMs.toInt())
            master.setRetries(config.retries)
            try {
                master.connect()
                return@withContext block(master)
            } catch (throwable: Throwable) {
                lastError = throwable
                if (attempt < totalAttempts - 1) {
                    delay(80L)
                }
            } finally {
                runCatching { master.disconnect() }
            }
        }

        throw IllegalStateException(
            "Modbus TCP 通信失败：service=${config.serviceId} host=${config.host}:${config.port} unit=${config.unitId}",
            lastError,
        )
    }
}

/**
 * Modbus TCP 运行时的 Koin 模块。
 *
 * 这里只扫描执行器实现；配置注册表由上层应用按具体 provider 显式组装，
 * 避免继续依赖隐式多绑定或手写 DSL `getAll()`。
 */
@Module
@ComponentScan("site.addzero.device.driver.modbus.tcp")
class ModbusTcpRuntimeKoinModule

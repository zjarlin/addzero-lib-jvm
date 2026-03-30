package site.addzero.device.driver.modbus.rtu

import com.fazecast.jSerialComm.SerialPort
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster
import com.ghgande.j2mod.modbus.net.AbstractSerialConnection
import com.ghgande.j2mod.modbus.procimg.SimpleRegister
import com.ghgande.j2mod.modbus.util.BitVector
import com.ghgande.j2mod.modbus.util.SerialParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

data class ModbusRtuEndpointConfig(
    val serviceId: String,
    val portPath: String,
    val unitId: Int,
    val baudRate: Int,
    val dataBits: Int = 8,
    val stopBits: Int = 1,
    val parity: ModbusSerialParity = ModbusSerialParity.NONE,
    val timeoutMs: Long,
    val retries: Int,
)

enum class ModbusSerialParity {
    NONE,
    EVEN,
    ODD,
}

interface ModbusRtuConfigProvider {
    val serviceId: String
    fun defaultConfig(): ModbusRtuEndpointConfig
}

class ModbusRtuConfigRegistry(
    providers: List<ModbusRtuConfigProvider>,
) {
    private val configs: Map<String, ModbusRtuEndpointConfig> =
        providers.associate { provider -> provider.serviceId to provider.defaultConfig() }

    fun require(serviceId: String): ModbusRtuEndpointConfig =
        configs[serviceId] ?: error("未找到 Modbus RTU 配置：$serviceId")
}

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

/**
 * 基于 j2mod 的 Modbus RTU 执行器。
 *
 * `J2mod` 是底层采用的 Java Modbus 协议库名，
 * 这个类的职责就是把项目里的高层 `ModbusRtuExecutor` 调用
 * 翻译成 j2mod 的串口主站读写动作。
 *
 * 当前职责边界只有两层：
 * 1. 根据 [ModbusRtuEndpointConfig] 创建串口主站连接
 * 2. 把读写请求映射到 j2mod 的功能码调用，并做基础重试
 *
 * 它不负责协议建模、寄存器地址定义或业务语义，
 * 那些都应该留在上层的 KSP 生成网关或业务契约里。
 */
@Single
class J2modModbusRtuExecutor : ModbusRtuExecutor {
    override suspend fun readCoils(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        withSerialMaster(config) { master ->
            master.readCoils(config.unitId, address, quantity).toBitStates(quantity)
        }

    override suspend fun readDiscreteInputs(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        withSerialMaster(config) { master ->
            master.readInputDiscretes(config.unitId, address, quantity).toBitStates(quantity)
        }

    override suspend fun readHoldingRegisters(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        withSerialMaster(config) { master ->
            master.readMultipleRegisters(config.unitId, address, quantity).map { register -> register.toUnsignedShort() }
        }

    override suspend fun readInputRegisters(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        withSerialMaster(config) { master ->
            master.readInputRegisters(config.unitId, address, quantity).map { register -> register.toUnsignedShort() }
        }

    override suspend fun writeSingleCoil(config: ModbusRtuEndpointConfig, address: Int, value: Boolean) {
        withSerialMaster(config) { master ->
            master.writeCoil(config.unitId, address, value)
        }
    }

    override suspend fun writeMultipleCoils(config: ModbusRtuEndpointConfig, address: Int, values: List<Boolean>) {
        withSerialMaster(config) { master ->
            val coils = BitVector(values.size)
            values.forEachIndexed { index, value ->
                coils.setBit(index, value)
            }
            master.writeMultipleCoils(config.unitId, address, coils)
        }
    }

    override suspend fun writeSingleRegister(config: ModbusRtuEndpointConfig, address: Int, value: Int) {
        withSerialMaster(config) { master ->
            master.writeSingleRegister(config.unitId, address, SimpleRegister(value and 0xFFFF))
        }
    }

    override suspend fun writeMultipleRegisters(config: ModbusRtuEndpointConfig, address: Int, values: List<Int>) {
        withSerialMaster(config) { master ->
            val registers = values.map { value -> SimpleRegister(value and 0xFFFF) }.toTypedArray()
            master.writeMultipleRegisters(config.unitId, address, registers)
        }
    }

    private suspend fun <T> withSerialMaster(
        config: ModbusRtuEndpointConfig,
        block: suspend (ModbusSerialMaster) -> T,
    ): T = withContext(Dispatchers.IO) {
        val totalAttempts = config.retries.coerceAtLeast(0) + 1
        var lastError: Throwable? = null
        repeat(totalAttempts) { attempt ->
            val master = createSerialMaster(config)
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
            "Modbus RTU 通信失败：service=${config.serviceId} port=${config.portPath} unit=${config.unitId}",
            lastError,
        )
    }
}

internal fun createSerialMaster(config: ModbusRtuEndpointConfig): ModbusSerialMaster =
    ModbusSerialMaster(createSerialParameters(config), config.timeoutMs.toInt())

internal fun createSerialParameters(config: ModbusRtuEndpointConfig): SerialParameters =
    SerialParameters(
        config.portPath,
        config.baudRate,
        SerialPort.FLOW_CONTROL_DISABLED,
        SerialPort.FLOW_CONTROL_DISABLED,
        config.dataBits,
        config.stopBits.toSerialStopBits(),
        config.parity.toSerialParity(),
        false,
    ).apply {
        encoding = "rtu"
        openDelay = 0
    }

private fun Int.toSerialStopBits(): Int =
    when (this) {
        2 -> SerialPort.TWO_STOP_BITS
        else -> SerialPort.ONE_STOP_BIT
    }

private fun ModbusSerialParity.toSerialParity(): Int =
    when (this) {
        ModbusSerialParity.NONE -> SerialPort.NO_PARITY
        ModbusSerialParity.EVEN -> SerialPort.EVEN_PARITY
        ModbusSerialParity.ODD -> SerialPort.ODD_PARITY
    }

class UnsupportedModbusRtuExecutor : ModbusRtuExecutor {
    override suspend fun readCoils(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        error("默认 RTU 执行器尚未接入真实串口实现：${config.serviceId}")

    override suspend fun readDiscreteInputs(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        error("默认 RTU 执行器尚未接入真实串口实现：${config.serviceId}")

    override suspend fun readHoldingRegisters(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        error("默认 RTU 执行器尚未接入真实串口实现：${config.serviceId}")

    override suspend fun readInputRegisters(config: ModbusRtuEndpointConfig, address: Int, quantity: Int): List<Int> =
        error("默认 RTU 执行器尚未接入真实串口实现：${config.serviceId}")

    override suspend fun writeSingleCoil(config: ModbusRtuEndpointConfig, address: Int, value: Boolean) {
        error("默认 RTU 执行器尚未接入真实串口实现：${config.serviceId}")
    }

    override suspend fun writeMultipleCoils(config: ModbusRtuEndpointConfig, address: Int, values: List<Boolean>) {
        error("默认 RTU 执行器尚未接入真实串口实现：${config.serviceId}")
    }

    override suspend fun writeSingleRegister(config: ModbusRtuEndpointConfig, address: Int, value: Int) {
        error("默认 RTU 执行器尚未接入真实串口实现：${config.serviceId}")
    }

    override suspend fun writeMultipleRegisters(config: ModbusRtuEndpointConfig, address: Int, values: List<Int>) {
        error("默认 RTU 执行器尚未接入真实串口实现：${config.serviceId}")
    }
}

/**
 * Modbus RTU 运行时的 Koin 模块。
 *
 * 统一通过 KCP 注解扫描收集运行时实现，不再暴露手写 DSL 模块。
 */
@Module
@ComponentScan("site.addzero.device.driver.modbus.rtu")
class ModbusRuntimeKoinModule

private fun BitVector.toBitStates(quantity: Int): List<Int> =
    List(quantity) { index -> if (getBit(index)) 1 else 0 }

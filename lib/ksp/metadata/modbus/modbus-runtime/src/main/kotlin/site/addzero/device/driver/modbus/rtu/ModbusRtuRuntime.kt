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

/**
 * 单个 Modbus RTU 终端的默认连接参数。
 *
 * @property serviceId 运行时内部使用的服务标识，通常由 KSP 生成网关引用
 * @property portPath 串口设备路径，例如 `/dev/ttyUSB0`
 * @property unitId Modbus 从站地址
 * @property baudRate 串口波特率
 * @property dataBits 数据位，默认 8 位
 * @property stopBits 停止位，目前按 1 或 2 位映射到底层串口参数
 * @property parity 串口奇偶校验位
 * @property timeoutMs 单次请求超时时间，单位毫秒
 * @property retries 失败后的额外重试次数，不包含首次请求
 */
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

/**
 * 运行时统一使用的串口校验位抽象。
 *
 * 这里不直接暴露 jSerialComm 常量，避免上层配置模型被底层串口库类型污染。
 */
enum class ModbusSerialParity {
    NONE,
    EVEN,
    ODD,
}

/**
 * 提供某个 RTU 服务默认配置的注册点。
 *
 * 一般由 KSP 生成代码或业务模块实现，再交给 [ModbusRtuConfigRegistry] 汇总。
 */
interface ModbusRtuConfigProvider {
    val serviceId: String
    fun defaultConfig(): ModbusRtuEndpointConfig
}

/**
 * 按 `serviceId` 汇总默认 RTU 配置的查询表。
 *
 * 运行时只做快速查找，不承担动态刷新、热更新或合并多层配置的职责。
 */
class ModbusRtuConfigRegistry(
    providers: List<ModbusRtuConfigProvider>,
) {
    /**
     * 启动期把 provider 列表收敛成只读索引，避免每次请求都遍历实现列表。
     */
    private val configs: Map<String, ModbusRtuEndpointConfig> =
        providers.associate { provider -> provider.serviceId to provider.defaultConfig() }

    /**
     * 返回指定服务的默认配置；如果缺失，直接暴露配置装配错误。
     */
    fun require(serviceId: String): ModbusRtuEndpointConfig =
        configs[serviceId] ?: error("未找到 Modbus RTU 配置：$serviceId")
}

/**
 * Modbus RTU 运行时执行契约。
 *
 * 上层网关只依赖这个抽象，不直接感知 j2mod、串口连接参数拼装或重试细节。
 * 读操作统一返回 `Int` 列表：
 * 1. 位类型使用 `0/1`
 * 2. 寄存器类型使用无符号 16 位数值展开后的 `Int`
 */
interface ModbusRtuExecutor {
    /**
     * 读取线圈状态，返回按请求顺序排列的 `0/1` 列表。
     */
    suspend fun readCoils(
        config: ModbusRtuEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int>

    /**
     * 读取离散输入，返回按请求顺序排列的 `0/1` 列表。
     */
    suspend fun readDiscreteInputs(
        config: ModbusRtuEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int>

    /**
     * 读取保持寄存器，返回无符号 16 位语义下的寄存器值。
     */
    suspend fun readHoldingRegisters(
        config: ModbusRtuEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int>

    /**
     * 读取输入寄存器，返回无符号 16 位语义下的寄存器值。
     */
    suspend fun readInputRegisters(
        config: ModbusRtuEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int>

    /**
     * 写入单个线圈。
     */
    suspend fun writeSingleCoil(
        config: ModbusRtuEndpointConfig,
        address: Int,
        value: Boolean,
    )

    /**
     * 从起始地址开始连续写入多个线圈。
     */
    suspend fun writeMultipleCoils(
        config: ModbusRtuEndpointConfig,
        address: Int,
        values: List<Boolean>,
    )

    /**
     * 写入单个保持寄存器，只保留低 16 位。
     */
    suspend fun writeSingleRegister(
        config: ModbusRtuEndpointConfig,
        address: Int,
        value: Int,
    )

    /**
     * 从起始地址开始连续写入多个保持寄存器，每项只保留低 16 位。
     */
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
        /**
         * 每次调用都独立创建和关闭主站连接，避免把串口生命周期泄漏到上层。
         *
         * 这里的重试是“重新建连后再执行”语义，而不是对同一个失效连接重复发送。
         */
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

/**
 * 根据项目里的配置模型创建 j2mod 主站实例。
 */
internal fun createSerialMaster(config: ModbusRtuEndpointConfig): ModbusSerialMaster =
    ModbusSerialMaster(createSerialParameters(config), config.timeoutMs.toInt())

/**
 * 把项目配置转换成 j2mod / jSerialComm 所需的串口参数对象。
 */
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

/**
 * 当前运行时只支持常见的 1 位和 2 位停止位；其他输入统一按 1 位处理。
 */
private fun Int.toSerialStopBits(): Int =
    when (this) {
        2 -> SerialPort.TWO_STOP_BITS
        else -> SerialPort.ONE_STOP_BIT
    }

/**
 * 把运行时的校验位枚举映射到底层串口库常量。
 */
private fun ModbusSerialParity.toSerialParity(): Int =
    when (this) {
        ModbusSerialParity.NONE -> SerialPort.NO_PARITY
        ModbusSerialParity.EVEN -> SerialPort.EVEN_PARITY
        ModbusSerialParity.ODD -> SerialPort.ODD_PARITY
    }

/**
 * 默认占位执行器。
 *
 * 当宿主应用还没有接入真实串口能力时，明确在调用点失败，避免静默降级成假成功。
 */
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

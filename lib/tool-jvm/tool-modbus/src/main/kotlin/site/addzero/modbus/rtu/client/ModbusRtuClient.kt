package site.addzero.modbus.rtu.client

import com.fazecast.jSerialComm.SerialPort
import com.ghgande.j2mod.modbus.Modbus
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster
import com.ghgande.j2mod.modbus.procimg.SimpleRegister
import com.ghgande.j2mod.modbus.util.BitVector
import com.ghgande.j2mod.modbus.util.SerialParameters
import java.io.Closeable
import site.addzero.modbus.ModbusToolException
import site.addzero.modbus.rtu.server.createRtuSerialParameters
import site.addzero.serial.SerialFlowControl
import site.addzero.serial.SerialParity
import site.addzero.serial.SerialStopBits

/**
 * Kotlin 风格的 Modbus RTU 客户端。
 *
 * 与 TCP 客户端不同，RTU 依赖实际串口参数是否和设备一致：
 * - 波特率
 * - 数据位
 * - 停止位
 * - 校验位
 *
 * 这些参数错误时，表面上仍可能“打开串口成功”，
 * 但最终会表现为无响应、CRC 错误或超时。
 */
class ModbusRtuClient private constructor(
    /**
     * 当前客户端的协议与串口配置。
     */
    val config: ModbusRtuClientConfig,
    /**
     * 会话工厂。
     *
     * 正式环境创建真实串口主站，测试环境创建 fake。
     */
    private val sessionFactory: ModbusRtuSessionFactory,
) : Closeable {
    /**
     * 非“按请求重连”模式下复用的会话。
     */
    private var sharedSession: ModbusRtuSession? = null

    /**
     * 生产环境入口，默认接入 j2mod 实现。
     */
    constructor(config: ModbusRtuClientConfig) : this(config, J2modModbusRtuSessionFactory)

    internal constructor(
        config: ModbusRtuClientConfig,
        sessionFactory: ModbusRtuSessionFactory,
        marker: Boolean = true,
    ) : this(config, sessionFactory)

    /**
     * 主动建立串口会话。
     *
     * 默认客户端是懒连接；如果你想在启动阶段就提前暴露串口占用或参数错误，
     * 可以显式调用这个方法。
     */
    @Synchronized
    fun connect() {
        if (config.reconnectPerRequest) {
            return
        }
        if (sharedSession == null) {
            sharedSession = sessionFactory.open(config)
        }
    }

    /**
     * 读取多个线圈。
     */
    @Synchronized
    fun readCoils(address: Int, count: Int): List<Boolean> =
        execute("读取 RTU 线圈失败：address=$address count=$count") { session ->
            validateAddressAndCount(address, count)
            session.readCoils(config.unitId, address, count)
        }

    /**
     * 读取单个线圈。
     */
    @Synchronized
    fun readCoil(address: Int): Boolean = readCoils(address, 1).first()

    /**
     * 读取多个离散输入。
     */
    @Synchronized
    fun readDiscreteInputs(address: Int, count: Int): List<Boolean> =
        execute("读取 RTU 离散输入失败：address=$address count=$count") { session ->
            validateAddressAndCount(address, count)
            session.readDiscreteInputs(config.unitId, address, count)
        }

    /**
     * 读取单个离散输入。
     */
    @Synchronized
    fun readDiscreteInput(address: Int): Boolean = readDiscreteInputs(address, 1).first()

    /**
     * 读取多个保持寄存器。
     */
    @Synchronized
    fun readHoldingRegisters(address: Int, count: Int): List<Int> =
        execute("读取 RTU 保持寄存器失败：address=$address count=$count") { session ->
            validateAddressAndCount(address, count)
            session.readHoldingRegisters(config.unitId, address, count)
        }

    /**
     * 读取单个保持寄存器。
     */
    @Synchronized
    fun readHoldingRegister(address: Int): Int = readHoldingRegisters(address, 1).first()

    /**
     * 读取多个输入寄存器。
     */
    @Synchronized
    fun readInputRegisters(address: Int, count: Int): List<Int> =
        execute("读取 RTU 输入寄存器失败：address=$address count=$count") { session ->
            validateAddressAndCount(address, count)
            session.readInputRegisters(config.unitId, address, count)
        }

    /**
     * 读取单个输入寄存器。
     */
    @Synchronized
    fun readInputRegister(address: Int): Int = readInputRegisters(address, 1).first()

    /**
     * 写入单个线圈。
     */
    @Synchronized
    fun writeSingleCoil(address: Int, value: Boolean) {
        execute("写入 RTU 单个线圈失败：address=$address value=$value") { session ->
            validateAddress(address)
            session.writeSingleCoil(config.unitId, address, value)
        }
    }

    /**
     * 批量写入线圈。
     */
    @Synchronized
    fun writeMultipleCoils(address: Int, values: List<Boolean>) {
        execute("批量写入 RTU 线圈失败：address=$address count=${values.size}") { session ->
            validateAddressAndCount(address, values.size)
            session.writeMultipleCoils(config.unitId, address, values)
        }
    }

    /**
     * 写入单个保持寄存器。
     */
    @Synchronized
    fun writeSingleRegister(address: Int, value: Int) {
        execute("写入 RTU 单个保持寄存器失败：address=$address value=$value") { session ->
            validateAddress(address)
            session.writeSingleRegister(config.unitId, address, value and 0xFFFF)
        }
    }

    /**
     * 批量写入保持寄存器。
     */
    @Synchronized
    fun writeMultipleRegisters(address: Int, values: List<Int>) {
        execute("批量写入 RTU 保持寄存器失败：address=$address count=${values.size}") { session ->
            validateAddressAndCount(address, values.size)
            session.writeMultipleRegisters(config.unitId, address, values.map { value -> value and 0xFFFF })
        }
    }

    @Synchronized
    override fun close() {
        /**
         * 关闭时不向上抛异常，避免资源释放阶段掩盖原始业务异常。
         */
        runCatching {
            sharedSession?.close()
        }
        sharedSession = null
    }

    private fun <T> execute(message: String, block: (ModbusRtuSession) -> T): T {
        /**
         * `retries` 表示“失败后再试几次”，
         * 所以总尝试次数需要 +1。
         */
        val totalAttempts = config.retries + 1
        var lastError: Throwable? = null
        for (attempt in 0 until totalAttempts) {
            var ephemeralSession: ModbusRtuSession? = null
            try {
                ephemeralSession =
                    if (config.reconnectPerRequest) {
                        /**
                         * 每次请求都新建一个临时会话，
                         * 用完后在 finally 里立即关闭。
                         */
                        sessionFactory.open(config)
                    } else {
                        if (sharedSession == null) {
                            /**
                             * 懒初始化共享会话，只在第一次真正需要时打开串口。
                             */
                            sharedSession = sessionFactory.open(config)
                        }
                        null
                    }
                val session = ephemeralSession ?: sharedSession!!
                return block(session)
            } catch (throwable: Throwable) {
                lastError = throwable
                if (!config.reconnectPerRequest) {
                    /**
                     * 共享会话一旦出错，直接丢弃重建，
                     * 避免下一次请求复用到半失效状态的串口连接。
                     */
                    runCatching { sharedSession?.close() }
                    sharedSession = null
                }
            } finally {
                if (ephemeralSession != null) {
                    runCatching { ephemeralSession.close() }
                }
            }
        }
        throw ModbusToolException(message, lastError)
    }

    private fun validateAddress(address: Int) {
        require(address >= 0) {
            "address 不能小于 0"
        }
    }

    private fun validateAddressAndCount(address: Int, count: Int) {
        validateAddress(address)
        require(count > 0) {
            "count 必须大于 0"
        }
    }
}

/**
 * RTU 会话工厂。
 *
 * 生产环境通过 j2mod 创建真实串口主站；
 * 单元测试可以替换成 fake 实现，避免依赖真实设备。
 */
internal fun interface ModbusRtuSessionFactory {
    /**
     * 按配置创建一个可直接执行 Modbus RTU 请求的会话。
     */
    fun open(config: ModbusRtuClientConfig): ModbusRtuSession
}

/**
 * 单个已建立的 RTU 主站会话。
 */
internal interface ModbusRtuSession : Closeable {
    /**
     * 下面这些接口和公开客户端 API 一一对应，
     * 作用是把“协议层流程”与“具体 j2mod 调用”隔离开。
     */
    fun readCoils(unitId: Int, address: Int, count: Int): List<Boolean>

    fun readDiscreteInputs(unitId: Int, address: Int, count: Int): List<Boolean>

    fun readHoldingRegisters(unitId: Int, address: Int, count: Int): List<Int>

    fun readInputRegisters(unitId: Int, address: Int, count: Int): List<Int>

    fun writeSingleCoil(unitId: Int, address: Int, value: Boolean)

    fun writeMultipleCoils(unitId: Int, address: Int, values: List<Boolean>)

    fun writeSingleRegister(unitId: Int, address: Int, value: Int)

    fun writeMultipleRegisters(unitId: Int, address: Int, values: List<Int>)
}

internal object J2modModbusRtuSessionFactory : ModbusRtuSessionFactory {
    override fun open(config: ModbusRtuClientConfig): ModbusRtuSession {
        /**
         * 串口参数的创建逻辑与服务端保持一致，
         * 避免客户端和服务端对同一份串口配置有不同解释。
         */
        val serialParameters = createRtuSerialParameters(config.serialConfig)
        val master =
            if (config.transmitDelayMs >= 0) {
                ModbusSerialMaster(serialParameters, config.requestTimeoutMs, config.transmitDelayMs)
            } else {
                ModbusSerialMaster(serialParameters, config.requestTimeoutMs)
            }
        return J2modModbusRtuSession(master)
    }
}

private class J2modModbusRtuSession(
    private val master: ModbusSerialMaster,
) : ModbusRtuSession {
    init {
        /**
         * 会话对象一旦创建成功，就保证已经连上串口，
         * 避免调用方拿到一个“半初始化”的会话。
         */
        master.connect()
    }

    override fun readCoils(unitId: Int, address: Int, count: Int): List<Boolean> {
        val bits = master.readCoils(unitId, address, count)
        return List(count) { index -> bits.getBit(index) }
    }

    override fun readDiscreteInputs(unitId: Int, address: Int, count: Int): List<Boolean> {
        val bits = master.readInputDiscretes(unitId, address, count)
        return List(count) { index -> bits.getBit(index) }
    }

    override fun readHoldingRegisters(unitId: Int, address: Int, count: Int): List<Int> =
        master.readMultipleRegisters(unitId, address, count).map { register -> register.value and 0xFFFF }

    override fun readInputRegisters(unitId: Int, address: Int, count: Int): List<Int> =
        master.readInputRegisters(unitId, address, count).map { register -> register.value and 0xFFFF }

    override fun writeSingleCoil(unitId: Int, address: Int, value: Boolean) {
        master.writeCoil(unitId, address, value)
    }

    override fun writeMultipleCoils(unitId: Int, address: Int, values: List<Boolean>) {
        /**
         * j2mod 批量写 coil 需要 `BitVector`，
         * 这里把更直观的 `List<Boolean>` 转成它需要的格式。
         */
        val coils = BitVector(values.size)
        values.forEachIndexed { index, value ->
            coils.setBit(index, value)
        }
        master.writeMultipleCoils(unitId, address, coils)
    }

    override fun writeSingleRegister(unitId: Int, address: Int, value: Int) {
        master.writeSingleRegister(unitId, address, SimpleRegister(value and 0xFFFF))
    }

    override fun writeMultipleRegisters(unitId: Int, address: Int, values: List<Int>) {
        master.writeMultipleRegisters(
            unitId,
            address,
            values.map { value -> SimpleRegister(value and 0xFFFF) }.toTypedArray(),
        )
    }

    override fun close() {
        master.disconnect()
    }
}

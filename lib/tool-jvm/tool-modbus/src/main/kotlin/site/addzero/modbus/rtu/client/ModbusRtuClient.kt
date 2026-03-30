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
    val config: ModbusRtuClientConfig,
    private val sessionFactory: ModbusRtuSessionFactory,
) : Closeable {
    private var sharedSession: ModbusRtuSession? = null

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
        runCatching {
            sharedSession?.close()
        }
        sharedSession = null
    }

    private fun <T> execute(message: String, block: (ModbusRtuSession) -> T): T {
        val totalAttempts = config.retries + 1
        var lastError: Throwable? = null
        for (attempt in 0 until totalAttempts) {
            var ephemeralSession: ModbusRtuSession? = null
            try {
                ephemeralSession =
                    if (config.reconnectPerRequest) {
                        sessionFactory.open(config)
                    } else {
                        if (sharedSession == null) {
                            sharedSession = sessionFactory.open(config)
                        }
                        null
                    }
                val session = ephemeralSession ?: sharedSession!!
                return block(session)
            } catch (throwable: Throwable) {
                lastError = throwable
                if (!config.reconnectPerRequest) {
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
    fun open(config: ModbusRtuClientConfig): ModbusRtuSession
}

/**
 * 单个已建立的 RTU 主站会话。
 */
internal interface ModbusRtuSession : Closeable {
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

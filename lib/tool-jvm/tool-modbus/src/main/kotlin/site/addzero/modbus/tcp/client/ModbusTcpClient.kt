package site.addzero.modbus.tcp.client

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster
import com.ghgande.j2mod.modbus.procimg.SimpleRegister
import com.ghgande.j2mod.modbus.util.BitVector
import java.io.Closeable
import site.addzero.modbus.toModbusToolException

/**
 * Kotlin 风格的 Modbus TCP 客户端。
 *
 * 相比直接暴露 j2mod，这里做了三件事：
 * 1. 收敛配置模型
 * 2. 返回 Kotlin 原生集合和值类型
 * 3. 把连接和异常处理统一到一个地方
 */
class ModbusTcpClient(
    /**
     * 当前客户端的连接参数。
     */
    val config: ModbusTcpClientConfig,
) : Closeable {
    /**
     * j2mod 提供的 TCP 主站对象。
     *
     * 连接管理、请求发送都由它完成，本类只做 Kotlin 化封装。
     */
    private val master =
        ModbusTCPMaster(
            config.host,
            config.port,
            config.timeoutMs,
            config.reconnectPerRequest,
            config.useRtuOverTcp,
        )

    val isConnected
        get() = master.isConnected

    /**
     * 主动建立连接。
     */
    @Synchronized
    fun connect() {
        runModbus("连接 Modbus TCP 服务端失败：${config.host}:${config.port}") {
            master.connect()
        }
    }

    /**
     * 读取多个线圈。
     */
    @Synchronized
    fun readCoils(address: Int, count: Int): List<Boolean> {
        validateAddressAndCount(address, count)
        return runModbus("读取线圈失败：address=$address count=$count", functionCode = 0x01) {
            ensureConnected()
            val bits = master.readCoils(config.unitId, address, count)
            List(count) { index -> bits.getBit(index) }
        }
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
    fun readDiscreteInputs(address: Int, count: Int): List<Boolean> {
        validateAddressAndCount(address, count)
        return runModbus("读取离散输入失败：address=$address count=$count", functionCode = 0x02) {
            ensureConnected()
            val bits = master.readInputDiscretes(config.unitId, address, count)
            List(count) { index -> bits.getBit(index) }
        }
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
    fun readHoldingRegisters(address: Int, count: Int): List<Int> {
        validateAddressAndCount(address, count)
        return runModbus("读取保持寄存器失败：address=$address count=$count", functionCode = 0x03) {
            ensureConnected()
            master.readMultipleRegisters(config.unitId, address, count).map { register -> register.getValue() and 0xFFFF }
        }
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
    fun readInputRegisters(address: Int, count: Int): List<Int> {
        validateAddressAndCount(address, count)
        return runModbus("读取输入寄存器失败：address=$address count=$count", functionCode = 0x04) {
            ensureConnected()
            master.readInputRegisters(config.unitId, address, count).map { register -> register.getValue() and 0xFFFF }
        }
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
        validateAddress(address)
        runModbus("写入单个线圈失败：address=$address value=$value", functionCode = 0x05) {
            ensureConnected()
            master.writeCoil(config.unitId, address, value)
        }
    }

    /**
     * 写入多个线圈。
     */
    @Synchronized
    fun writeMultipleCoils(address: Int, values: List<Boolean>) {
        validateAddressAndCount(address, values.size)
        runModbus("批量写入线圈失败：address=$address count=${values.size}", functionCode = 0x0F) {
            ensureConnected()
            val coils = BitVector(values.size)
            values.forEachIndexed { index, value ->
                coils.setBit(index, value)
            }
            master.writeMultipleCoils(config.unitId, address, coils)
        }
    }

    /**
     * 写入单个保持寄存器。
     */
    @Synchronized
    fun writeSingleRegister(address: Int, value: Int) {
        validateAddress(address)
        runModbus("写入单个保持寄存器失败：address=$address value=$value", functionCode = 0x06) {
            ensureConnected()
            master.writeSingleRegister(config.unitId, address, SimpleRegister(value and 0xFFFF))
        }
    }

    /**
     * 写入多个保持寄存器。
     */
    @Synchronized
    fun writeMultipleRegisters(address: Int, values: List<Int>) {
        validateAddressAndCount(address, values.size)
        runModbus("批量写入保持寄存器失败：address=$address count=${values.size}", functionCode = 0x10) {
            ensureConnected()
            master.writeMultipleRegisters(
                config.unitId,
                address,
                values.map { value -> SimpleRegister(value and 0xFFFF) }.toTypedArray(),
            )
        }
    }

    /**
     * 对单个保持寄存器执行 mask write。
     */
    @Synchronized
    fun maskWriteRegister(address: Int, andMask: Int, orMask: Int): Boolean {
        validateAddress(address)
        return runModbus("Mask Write Register 失败：address=$address", functionCode = 0x16) {
            ensureConnected()
            master.maskWriteRegister(config.unitId, address, andMask and 0xFFFF, orMask and 0xFFFF)
        }
    }

    /**
     * 断开连接。
     */
    @Synchronized
    override fun close() {
        runCatching {
            master.disconnect()
        }
    }

    private fun ensureConnected() {
        /**
         * j2mod 支持“先 new，再在第一次请求时 connect”，
         * 这里把这层惰性连接逻辑隐藏起来。
         */
        if (!master.isConnected) {
            master.connect()
        }
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

    private fun <T> runModbus(
        message: String,
        functionCode: Int? = null,
        block: () -> T,
    ): T =
        try {
            block()
        } catch (throwable: Throwable) {
            /**
             * 统一包成项目自己的异常，避免把 j2mod 的细碎异常类型直接暴露给业务层。
             */
            throw throwable.toModbusToolException(message, functionCode)
        }
}

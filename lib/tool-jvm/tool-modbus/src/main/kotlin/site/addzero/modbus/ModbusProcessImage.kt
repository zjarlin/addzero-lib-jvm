package site.addzero.modbus

import com.ghgande.j2mod.modbus.procimg.SimpleDigitalIn
import com.ghgande.j2mod.modbus.procimg.SimpleDigitalOut
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage
import com.ghgande.j2mod.modbus.procimg.SimpleRegister

/**
 * 单个 unit id 对应的 process image 封装。
 *
 * 这个类型本身不关心底层是 TCP 还是 RTU，
 * 它只负责把 j2mod 的 `SimpleProcessImage` 包装成更直接的 Kotlin API。
 */
class ModbusProcessImage internal constructor(
    /**
     * 当前寄存器映像所属的从站地址。
     */
    val unitId: Int,
    /**
     * 真正存储点位数据的 j2mod 对象。
     *
     * 对外隐藏这个实现细节，调用方只操作更直接的 Kotlin API。
     */
    internal val delegate: SimpleProcessImage,
) {
    /**
     * 设置单个线圈。
     */
    fun setCoil(address: Int, value: Boolean) {
        validateAddress(address)
        /**
         * j2mod 的 process image 通过“按地址放对象”的方式维护点位，
         * 因此这里每次写入时都重新放入当前地址的最新值对象。
         */
        delegate.addDigitalOut(address, SimpleDigitalOut(value))
    }

    /**
     * 批量设置线圈。
     */
    fun setCoils(startAddress: Int, values: List<Boolean>) {
        validateAddressAndCount(startAddress, values.size)
        values.forEachIndexed { index, value ->
            setCoil(startAddress + index, value)
        }
    }

    /**
     * 读取单个线圈。
     */
    fun getCoil(address: Int): Boolean {
        validateAddress(address)
        return delegate.getDigitalOut(address).isSet
    }

    /**
     * 读取多个线圈。
     */
    fun getCoils(startAddress: Int, count: Int): List<Boolean> {
        validateAddressAndCount(startAddress, count)
        return List(count) { offset -> getCoil(startAddress + offset) }
    }

    /**
     * 设置单个离散输入。
     */
    fun setDiscreteInput(address: Int, value: Boolean) {
        validateAddress(address)
        delegate.addDigitalIn(address, SimpleDigitalIn(value))
    }

    /**
     * 批量设置离散输入。
     */
    fun setDiscreteInputs(startAddress: Int, values: List<Boolean>) {
        validateAddressAndCount(startAddress, values.size)
        values.forEachIndexed { index, value ->
            setDiscreteInput(startAddress + index, value)
        }
    }

    /**
     * 读取单个离散输入。
     */
    fun getDiscreteInput(address: Int): Boolean {
        validateAddress(address)
        return delegate.getDigitalIn(address).isSet
    }

    /**
     * 读取多个离散输入。
     */
    fun getDiscreteInputs(startAddress: Int, count: Int): List<Boolean> {
        validateAddressAndCount(startAddress, count)
        return List(count) { offset -> getDiscreteInput(startAddress + offset) }
    }

    /**
     * 设置单个保持寄存器。
     */
    fun setHoldingRegister(address: Int, value: Int) {
        validateAddress(address)
        /**
         * Modbus 寄存器本质是 16 位无符号值，
         * 这里统一截断到低 16 位，避免上层传入大于 0xFFFF 的整数时语义不清。
         */
        delegate.addRegister(address, SimpleRegister(value and 0xFFFF))
    }

    /**
     * 批量设置保持寄存器。
     */
    fun setHoldingRegisters(startAddress: Int, values: List<Int>) {
        validateAddressAndCount(startAddress, values.size)
        values.forEachIndexed { index, value ->
            setHoldingRegister(startAddress + index, value)
        }
    }

    /**
     * 读取单个保持寄存器。
     */
    fun getHoldingRegister(address: Int): Int {
        validateAddress(address)
        return delegate.getRegister(address).value and 0xFFFF
    }

    /**
     * 读取多个保持寄存器。
     */
    fun getHoldingRegisters(startAddress: Int, count: Int): List<Int> {
        validateAddressAndCount(startAddress, count)
        return List(count) { offset -> getHoldingRegister(startAddress + offset) }
    }

    /**
     * 设置单个输入寄存器。
     */
    fun setInputRegister(address: Int, value: Int) {
        validateAddress(address)
        delegate.addInputRegister(address, SimpleInputRegister(value and 0xFFFF))
    }

    /**
     * 批量设置输入寄存器。
     */
    fun setInputRegisters(startAddress: Int, values: List<Int>) {
        validateAddressAndCount(startAddress, values.size)
        values.forEachIndexed { index, value ->
            setInputRegister(startAddress + index, value)
        }
    }

    /**
     * 读取单个输入寄存器。
     */
    fun getInputRegister(address: Int): Int {
        validateAddress(address)
        return delegate.getInputRegister(address).value and 0xFFFF
    }

    /**
     * 读取多个输入寄存器。
     */
    fun getInputRegisters(startAddress: Int, count: Int): List<Int> {
        validateAddressAndCount(startAddress, count)
        return List(count) { offset -> getInputRegister(startAddress + offset) }
    }

    private fun validateAddress(address: Int) {
        require(address >= 0) {
            "address 不能小于 0"
        }
    }

    /**
     * 对“起始地址 + 连续数量”这种调用统一做参数兜底。
     */
    private fun validateAddressAndCount(address: Int, count: Int) {
        validateAddress(address)
        require(count > 0) {
            "count 必须大于 0"
        }
    }
}

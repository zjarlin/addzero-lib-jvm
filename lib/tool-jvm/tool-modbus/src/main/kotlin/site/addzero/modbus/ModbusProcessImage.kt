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
    val unitId: Int,
    internal val delegate: SimpleProcessImage,
) {
    /**
     * 设置单个线圈。
     */
    fun setCoil(address: Int, value: Boolean) {
        validateAddress(address)
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

    private fun validateAddressAndCount(address: Int, count: Int) {
        validateAddress(address)
        require(count > 0) {
            "count 必须大于 0"
        }
    }
}

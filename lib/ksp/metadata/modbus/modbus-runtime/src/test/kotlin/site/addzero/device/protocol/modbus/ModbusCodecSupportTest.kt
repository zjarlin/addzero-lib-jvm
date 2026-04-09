package site.addzero.device.protocol.modbus

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import site.addzero.device.protocol.modbus.model.ModbusCodec

class ModbusCodecSupportTest {
    @Test
    fun decodeIntSupportsU8RegisterValues() {
        val decoded = ModbusCodecSupport.decodeInt(ModbusCodec.U8, listOf(0x01FE), 0)

        assertEquals(0xFE, decoded)
    }

    @Test
    fun encodeAndDecodeByteArrayUsesRegisterPacking() {
        val bytes = byteArrayOf(0x11, 0x22, 0x33, 0x44, 0x55)

        val registers = ModbusCodecSupport.encodeByteArray(ModbusCodec.BYTE_ARRAY, bytes, bytes.size)
        val decoded = ModbusCodecSupport.decodeByteArray(ModbusCodec.BYTE_ARRAY, registers, 0, bytes.size)

        assertEquals(listOf(0x1122, 0x3344, 0x5500), registers)
        assertContentEquals(bytes, decoded)
    }

    @Test
    fun encodeValueRejectsOutOfRangeU8() {
        assertFailsWith<IllegalArgumentException> {
            ModbusCodecSupport.encodeValue(ModbusCodec.U8, "256")
        }
    }
}

package site.addzero.device.protocol.modbus.ksp.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ModbusCodegenModeTest {
    @Test
    fun parseDefaultsToServerWhenMissing() {
        assertEquals(setOf(ModbusCodegenMode.SERVER), ModbusCodegenMode.parse(null))
        assertEquals(setOf(ModbusCodegenMode.SERVER), ModbusCodegenMode.parse(""))
    }

    @Test
    fun parseSupportsCommaSeparatedMultiSelect() {
        assertEquals(
            linkedSetOf(ModbusCodegenMode.SERVER, ModbusCodegenMode.CONTRACT),
            ModbusCodegenMode.parse("server,contract"),
        )
        assertEquals(
            linkedSetOf(ModbusCodegenMode.CONTRACT, ModbusCodegenMode.SERVER),
            ModbusCodegenMode.parse("contract, server"),
        )
    }

    @Test
    fun parseRejectsUnknownMode() {
        assertFailsWith<IllegalStateException> {
            ModbusCodegenMode.parse("server,unknown")
        }
    }
}

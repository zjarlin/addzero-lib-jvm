package site.addzero.device.protocol.modbus.ksp.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ModbusTransportKindTest {
    @Test
    fun parseConfiguredSupportsCommaSeparatedTransportIds() {
        assertEquals(
            linkedSetOf(ModbusTransportKind.RTU, ModbusTransportKind.TCP),
            ModbusTransportKind.parseConfigured("rtu,tcp"),
        )
        assertEquals(
            linkedSetOf(ModbusTransportKind.TCP, ModbusTransportKind.RTU),
            ModbusTransportKind.parseConfigured("tcp, RTU"),
        )
    }

    @Test
    fun resolveConfiguredOrDefaultFallsBackToProviderDefault() {
        assertEquals(
            linkedSetOf(ModbusTransportKind.RTU),
            ModbusTransportKind.resolveConfiguredOrDefault(null, ModbusTransportKind.RTU),
        )
        assertEquals(
            linkedSetOf(ModbusTransportKind.TCP),
            ModbusTransportKind.resolveConfiguredOrDefault("", ModbusTransportKind.TCP),
        )
    }

    @Test
    fun parseConfiguredRejectsUnknownTransport() {
        assertFailsWith<IllegalStateException> {
            ModbusTransportKind.parseConfigured("rtu,mqtt")
        }
    }
}

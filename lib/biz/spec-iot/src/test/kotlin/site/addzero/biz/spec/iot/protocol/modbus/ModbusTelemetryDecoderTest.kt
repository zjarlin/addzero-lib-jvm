package site.addzero.biz.spec.iot.protocol.modbus

import org.junit.jupiter.api.Test
import site.addzero.biz.spec.iot.IotThingRef
import site.addzero.biz.spec.iot.IotValueType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModbusTelemetryDecoderTest {

    @Test
    fun shouldDecodeFloatIntAndBooleanValues() {
        val rawValues = linkedMapOf<String, Any?>(
            "float-point" to 12.5f,
            "int-point" to floatFromLittleEndianBytes(byteArrayOf(0x34, 0x12, 0x00, 0x00)),
            "bool-register-point" to floatFromLittleEndianBytes(byteArrayOf(0x01, 0x00, 0x00, 0x00)),
            "coil-point" to true,
        )

        val decoder = ModbusTelemetryDecoder()
        val values = decoder.decode(
            rawValues,
            listOf(
                ModbusPointBinding(
                    "float-point",
                    IotThingRef.of("product", "demo-product"),
                    IotThingRef.of("device", "device-1"),
                    1,
                    0,
                    ModbusRegisterType.HOLDING_REGISTER,
                    "temperature",
                    IotValueType.FLOAT32,
                ),
                ModbusPointBinding(
                    "int-point",
                    IotThingRef.of("product", "demo-product"),
                    IotThingRef.of("device", "device-1"),
                    1,
                    1,
                    ModbusRegisterType.HOLDING_REGISTER,
                    "counter",
                    IotValueType.INT32,
                ),
                ModbusPointBinding(
                    "bool-register-point",
                    IotThingRef.of("product", "demo-product"),
                    IotThingRef.of("device", "device-1"),
                    1,
                    2,
                    ModbusRegisterType.HOLDING_REGISTER,
                    "alarm",
                    IotValueType.BOOLEAN,
                ),
                ModbusPointBinding(
                    "coil-point",
                    IotThingRef.of("product", "demo-product"),
                    IotThingRef.of("device", "device-1"),
                    1,
                    3,
                    ModbusRegisterType.COIL_STATUS,
                    "running",
                    IotValueType.BOOLEAN,
                ),
            ),
        )

        assertEquals(12.5f, values.getValue("temperature").value as Float, 0.0001f)
        assertEquals(0x1234, values.getValue("counter").value)
        assertEquals(true, values.getValue("alarm").value)
        assertEquals(true, values.getValue("running").value)

        val report = decoder.decodeReport(
            IotThingRef.of("product", "demo-product"),
            IotThingRef.of("device", "device-1"),
            LocalDateTime.of(2026, 3, 16, 15, 0),
            rawValues,
            listOf(
                ModbusPointBinding(
                    "float-point",
                    IotThingRef.of("product", "demo-product"),
                    IotThingRef.of("device", "device-1"),
                    1,
                    0,
                    ModbusRegisterType.HOLDING_REGISTER,
                    "temperature",
                    IotValueType.FLOAT32,
                ),
                ModbusPointBinding(
                    "int-point",
                    IotThingRef.of("product", "demo-product"),
                    IotThingRef.of("device", "device-1"),
                    1,
                    1,
                    ModbusRegisterType.HOLDING_REGISTER,
                    "counter",
                    IotValueType.INT32,
                ),
                ModbusPointBinding(
                    "coil-point",
                    IotThingRef.of("product", "demo-product"),
                    IotThingRef.of("device", "device-1"),
                    1,
                    3,
                    ModbusRegisterType.COIL_STATUS,
                    "running",
                    IotValueType.BOOLEAN,
                ),
            ),
        )
        assertEquals(3, report.values.size)
        assertTrue(report.values.containsKey("temperature"))
    }

    private fun floatFromLittleEndianBytes(bytes: ByteArray): Float {
        val buffer = ByteBuffer.wrap(bytes)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        return buffer.float
    }
}

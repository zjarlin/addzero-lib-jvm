package site.addzero.biz.spec.iot.protocol.s7

import org.junit.jupiter.api.Test
import site.addzero.biz.spec.iot.IotThingRef
import site.addzero.biz.spec.iot.IotValueType
import java.nio.ByteBuffer
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class S7TelemetryDecoderTest {

    @Test
    fun shouldDecodeIntFloatAndBooleanValues() {
        val buffer = ByteBuffer.allocate(9)
        buffer.putInt(0, 42)
        buffer.putFloat(4, 12.5f)
        buffer.put(8, 0b00000100)

        val decoder = S7TelemetryDecoder()
        val values = decoder.decode(
            buffer.array(),
            listOf(
                S7PropertyBinding("counter", IotValueType.INT32, 0, 0),
                S7PropertyBinding("temperature", IotValueType.FLOAT32, 4, 0),
                S7PropertyBinding("running", IotValueType.BOOLEAN, 8, 2),
            ),
        )

        assertEquals(42, values.getValue("counter").value)
        assertEquals(12.5f, values.getValue("temperature").value as Float, 0.0001f)
        assertEquals(true, values.getValue("running").value)

        val report = decoder.decodeReport(
            IotThingRef.of("product", "demo-product"),
            IotThingRef.of("device", "device-1"),
            LocalDateTime.of(2026, 3, 16, 14, 0),
            buffer.array(),
            listOf(
                S7PropertyBinding("counter", IotValueType.INT32, 0, 0),
                S7PropertyBinding("temperature", IotValueType.FLOAT32, 4, 0),
                S7PropertyBinding("running", IotValueType.BOOLEAN, 8, 2),
            ),
        )
        assertEquals(3, report.values.size)
        assertTrue(report.values.containsKey("running"))
    }
}

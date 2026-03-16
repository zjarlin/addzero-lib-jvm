package site.addzero.biz.spec.iot.protocol.s7;

import org.junit.jupiter.api.Test;
import site.addzero.biz.spec.iot.IotThingRef;
import site.addzero.biz.spec.iot.IotValueType;
import site.addzero.biz.spec.iot.TelemetryReport;
import site.addzero.biz.spec.iot.TelemetryValue;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class S7TelemetryDecoderTest {

    @Test
    void shouldDecodeIntFloatAndBooleanValues() {
        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.putInt(0, 42);
        buffer.putFloat(4, 12.5f);
        buffer.put(8, (byte) 0b00000100);

        S7TelemetryDecoder decoder = new S7TelemetryDecoder();
        Map<String, TelemetryValue> values = decoder.decode(
                buffer.array(),
                Arrays.asList(
                        new S7PropertyBinding("counter", IotValueType.INT32, 0, 0),
                        new S7PropertyBinding("temperature", IotValueType.FLOAT32, 4, 0),
                        new S7PropertyBinding("running", IotValueType.BOOLEAN, 8, 2)
                )
        );

        assertEquals(42, values.get("counter").getValue());
        assertEquals(12.5f, (Float) values.get("temperature").getValue(), 0.0001f);
        assertEquals(Boolean.TRUE, values.get("running").getValue());

        TelemetryReport report = decoder.decodeReport(
                IotThingRef.of("product", "demo-product"),
                IotThingRef.of("device", "device-1"),
                LocalDateTime.of(2026, 3, 16, 14, 0),
                buffer.array(),
                Arrays.asList(
                        new S7PropertyBinding("counter", IotValueType.INT32, 0, 0),
                        new S7PropertyBinding("temperature", IotValueType.FLOAT32, 4, 0),
                        new S7PropertyBinding("running", IotValueType.BOOLEAN, 8, 2)
                )
        );
        assertEquals(3, report.getValues().size());
        assertTrue(report.getValues().containsKey("running"));
    }
}

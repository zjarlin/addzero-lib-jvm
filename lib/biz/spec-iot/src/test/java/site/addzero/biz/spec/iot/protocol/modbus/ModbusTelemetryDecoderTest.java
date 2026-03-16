package site.addzero.biz.spec.iot.protocol.modbus;

import org.junit.jupiter.api.Test;
import site.addzero.biz.spec.iot.IotThingRef;
import site.addzero.biz.spec.iot.IotValueType;
import site.addzero.biz.spec.iot.TelemetryReport;
import site.addzero.biz.spec.iot.TelemetryValue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModbusTelemetryDecoderTest {

    @Test
    void shouldDecodeFloatIntAndBooleanValues() {
        Map<String, Object> rawValues = new LinkedHashMap<String, Object>();
        rawValues.put("float-point", 12.5f);
        rawValues.put("int-point", floatFromLittleEndianBytes(new byte[]{0x34, 0x12, 0x00, 0x00}));
        rawValues.put("bool-register-point", floatFromLittleEndianBytes(new byte[]{0x01, 0x00, 0x00, 0x00}));
        rawValues.put("coil-point", Boolean.TRUE);

        ModbusTelemetryDecoder decoder = new ModbusTelemetryDecoder();
        Map<String, TelemetryValue> values = decoder.decode(
                rawValues,
                Arrays.asList(
                        new ModbusPointBinding("float-point", IotThingRef.of("product", "demo-product"), IotThingRef.of("device", "device-1"), 1, 0, ModbusRegisterType.HOLDING_REGISTER, "temperature", IotValueType.FLOAT32),
                        new ModbusPointBinding("int-point", IotThingRef.of("product", "demo-product"), IotThingRef.of("device", "device-1"), 1, 1, ModbusRegisterType.HOLDING_REGISTER, "counter", IotValueType.INT32),
                        new ModbusPointBinding("bool-register-point", IotThingRef.of("product", "demo-product"), IotThingRef.of("device", "device-1"), 1, 2, ModbusRegisterType.HOLDING_REGISTER, "alarm", IotValueType.BOOLEAN),
                        new ModbusPointBinding("coil-point", IotThingRef.of("product", "demo-product"), IotThingRef.of("device", "device-1"), 1, 3, ModbusRegisterType.COIL_STATUS, "running", IotValueType.BOOLEAN)
                )
        );

        assertEquals(12.5f, (Float) values.get("temperature").getValue(), 0.0001f);
        assertEquals(0x1234, values.get("counter").getValue());
        assertEquals(Boolean.TRUE, values.get("alarm").getValue());
        assertEquals(Boolean.TRUE, values.get("running").getValue());

        TelemetryReport report = decoder.decodeReport(
                IotThingRef.of("product", "demo-product"),
                IotThingRef.of("device", "device-1"),
                LocalDateTime.of(2026, 3, 16, 15, 0),
                rawValues,
                Arrays.asList(
                        new ModbusPointBinding("float-point", IotThingRef.of("product", "demo-product"), IotThingRef.of("device", "device-1"), 1, 0, ModbusRegisterType.HOLDING_REGISTER, "temperature", IotValueType.FLOAT32),
                        new ModbusPointBinding("int-point", IotThingRef.of("product", "demo-product"), IotThingRef.of("device", "device-1"), 1, 1, ModbusRegisterType.HOLDING_REGISTER, "counter", IotValueType.INT32),
                        new ModbusPointBinding("coil-point", IotThingRef.of("product", "demo-product"), IotThingRef.of("device", "device-1"), 1, 3, ModbusRegisterType.COIL_STATUS, "running", IotValueType.BOOLEAN)
                )
        );
        assertEquals(3, report.getValues().size());
        assertTrue(report.getValues().containsKey("temperature"));
    }

    private static float floatFromLittleEndianBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getFloat();
    }
}

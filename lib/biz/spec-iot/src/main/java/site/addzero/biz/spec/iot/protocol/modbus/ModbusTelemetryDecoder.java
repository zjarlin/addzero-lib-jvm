package site.addzero.biz.spec.iot.protocol.modbus;

import site.addzero.biz.spec.iot.IotThingRef;
import site.addzero.biz.spec.iot.IotValueType;
import site.addzero.biz.spec.iot.TelemetryReport;
import site.addzero.biz.spec.iot.TelemetryValue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Decodes raw Modbus locator values into typed telemetry.
 */
public final class ModbusTelemetryDecoder {

    public Map<String, TelemetryValue> decode(Map<String, Object> rawValues, List<ModbusPointBinding> bindings) {
        Map<String, TelemetryValue> decoded = new LinkedHashMap<String, TelemetryValue>();
        for (ModbusPointBinding binding : bindings) {
            Object rawValue = rawValues.get(binding.getPointId());
            if (rawValue == null) {
                continue;
            }
            Object value = decodeValue(binding, rawValue);
            if (value != null) {
                decoded.put(binding.getIdentifier(), new TelemetryValue(binding.getIdentifier(), binding.getValueType(), value));
            }
        }
        return decoded;
    }

    public TelemetryReport decodeReport(
            IotThingRef schemaRef,
            IotThingRef sourceRef,
            LocalDateTime reportTime,
            Map<String, Object> rawValues,
            List<ModbusPointBinding> bindings
    ) {
        Map<String, TelemetryValue> decoded = decode(rawValues, bindings);
        TelemetryReport.Builder builder = TelemetryReport.builder()
                .schemaRef(schemaRef)
                .sourceRef(sourceRef)
                .reportTime(reportTime);
        for (TelemetryValue value : decoded.values()) {
            builder.addValue(value);
        }
        return builder.build();
    }

    private Object decodeValue(ModbusPointBinding binding, Object rawValue) {
        if (binding.getValueType() == IotValueType.FLOAT32) {
            if (rawValue instanceof Number) {
                return ((Number) rawValue).floatValue();
            }
            return null;
        }
        if (binding.getValueType() == IotValueType.INT32) {
            if (rawValue instanceof Number) {
                return extractIntFromSwappedFloat(((Number) rawValue).floatValue());
            }
            return null;
        }
        if (binding.getValueType() == IotValueType.BOOLEAN) {
            if (rawValue instanceof Boolean) {
                return rawValue;
            }
            if (rawValue instanceof Number) {
                return extractBooleanFromSwappedFloat(((Number) rawValue).floatValue());
            }
            return null;
        }
        return null;
    }

    private int extractIntFromSwappedFloat(float value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putFloat(value);
        byte[] bytes = buffer.array();
        int highByte = bytes[1] & 0xFF;
        int lowByte = bytes[0] & 0xFF;
        return (highByte << 8) | lowByte;
    }

    private boolean extractBooleanFromSwappedFloat(float value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putFloat(value);
        byte[] bytes = buffer.array();
        return (bytes[0] & 0x01) == 1;
    }
}

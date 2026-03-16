package site.addzero.biz.spec.iot.protocol.s7;

import site.addzero.biz.spec.iot.IotThingRef;
import site.addzero.biz.spec.iot.IotValueType;
import site.addzero.biz.spec.iot.TelemetryReport;
import site.addzero.biz.spec.iot.TelemetryValue;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Decodes S7 byte payloads into telemetry values.
 */
public final class S7TelemetryDecoder {

    public Map<String, TelemetryValue> decode(byte[] payload, List<S7PropertyBinding> bindings) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        Map<String, TelemetryValue> decoded = new LinkedHashMap<String, TelemetryValue>();
        for (S7PropertyBinding binding : bindings) {
            Object value = decodeValue(buffer, binding);
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
            byte[] payload,
            List<S7PropertyBinding> bindings
    ) {
        Map<String, TelemetryValue> decoded = decode(payload, bindings);
        TelemetryReport.Builder builder = TelemetryReport.builder()
                .schemaRef(schemaRef)
                .sourceRef(sourceRef)
                .reportTime(reportTime);
        for (TelemetryValue value : decoded.values()) {
            builder.addValue(value);
        }
        return builder.build();
    }

    private Object decodeValue(ByteBuffer buffer, S7PropertyBinding binding) {
        if (binding.getValueType() == IotValueType.INT32) {
            return buffer.getInt(binding.getByteOffset());
        }
        if (binding.getValueType() == IotValueType.FLOAT32) {
            return buffer.getFloat(binding.getByteOffset());
        }
        if (binding.getValueType() == IotValueType.BOOLEAN) {
            return (buffer.get(binding.getByteOffset()) & (1 << binding.getBitOffset())) != 0;
        }
        return null;
    }
}

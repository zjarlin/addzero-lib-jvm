package site.addzero.biz.spec.iot;

/**
 * Single telemetry value in a report.
 */
public final class TelemetryValue {

    private final String identifier;
    private final IotValueType valueType;
    private final Object value;

    public TelemetryValue(String identifier, IotValueType valueType, Object value) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("identifier must not be blank");
        }
        if (valueType == null) {
            throw new IllegalArgumentException("valueType must not be null");
        }
        this.identifier = identifier.trim();
        this.valueType = valueType;
        this.value = value;
    }

    public String getIdentifier() {
        return identifier;
    }

    public IotValueType getValueType() {
        return valueType;
    }

    public Object getValue() {
        return value;
    }
}

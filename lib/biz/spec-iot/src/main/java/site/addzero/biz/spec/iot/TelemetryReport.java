package site.addzero.biz.spec.iot;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standardized telemetry envelope for protocol decoders and SQL builders.
 */
public final class TelemetryReport {

    private final IotThingRef schemaRef;
    private final IotThingRef sourceRef;
    private final LocalDateTime reportTime;
    private final Map<String, TelemetryValue> values;

    private TelemetryReport(Builder builder) {
        if (builder.schemaRef == null) {
            throw new IllegalArgumentException("schemaRef must not be null");
        }
        if (builder.sourceRef == null) {
            throw new IllegalArgumentException("sourceRef must not be null");
        }
        if (builder.reportTime == null) {
            throw new IllegalArgumentException("reportTime must not be null");
        }
        if (builder.values.isEmpty()) {
            throw new IllegalArgumentException("telemetry values must not be empty");
        }
        this.schemaRef = builder.schemaRef;
        this.sourceRef = builder.sourceRef;
        this.reportTime = builder.reportTime;
        this.values = Collections.unmodifiableMap(new LinkedHashMap<String, TelemetryValue>(builder.values));
    }

    public static Builder builder() {
        return new Builder();
    }

    public IotThingRef getSchemaRef() {
        return schemaRef;
    }

    public IotThingRef getSourceRef() {
        return sourceRef;
    }

    public LocalDateTime getReportTime() {
        return reportTime;
    }

    public Map<String, TelemetryValue> getValues() {
        return values;
    }

    public static final class Builder {

        private IotThingRef schemaRef;
        private IotThingRef sourceRef;
        private LocalDateTime reportTime;
        private final Map<String, TelemetryValue> values = new LinkedHashMap<String, TelemetryValue>();

        public Builder schemaRef(IotThingRef schemaRef) {
            this.schemaRef = schemaRef;
            return this;
        }

        public Builder sourceRef(IotThingRef sourceRef) {
            this.sourceRef = sourceRef;
            return this;
        }

        public Builder reportTime(LocalDateTime reportTime) {
            this.reportTime = reportTime;
            return this;
        }

        public Builder addValue(TelemetryValue value) {
            if (value == null) {
                throw new IllegalArgumentException("value must not be null");
            }
            this.values.put(value.getIdentifier(), value);
            return this;
        }

        public TelemetryReport build() {
            return new TelemetryReport(this);
        }
    }
}

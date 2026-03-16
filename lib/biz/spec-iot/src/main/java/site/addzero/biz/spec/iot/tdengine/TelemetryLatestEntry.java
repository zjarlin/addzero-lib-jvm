package site.addzero.biz.spec.iot.tdengine;

import site.addzero.biz.spec.iot.IotThingRef;

/**
 * Latest-value query output DTO.
 */
public final class TelemetryLatestEntry {

    private final IotThingRef sourceRef;
    private final String identifier;
    private final Object value;
    private final Long updateTimeEpochMillis;

    public TelemetryLatestEntry(IotThingRef sourceRef, String identifier, Object value, Long updateTimeEpochMillis) {
        this.sourceRef = sourceRef;
        this.identifier = identifier;
        this.value = value;
        this.updateTimeEpochMillis = updateTimeEpochMillis;
    }

    public IotThingRef getSourceRef() {
        return sourceRef;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Object getValue() {
        return value;
    }

    public Long getUpdateTimeEpochMillis() {
        return updateTimeEpochMillis;
    }
}

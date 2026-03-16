package site.addzero.biz.spec.iot.tdengine;

/**
 * History query output DTO.
 */
public final class TelemetryHistoryEntry {

    private final String identifier;
    private final Object value;
    private final Long updateTimeEpochMillis;

    public TelemetryHistoryEntry(String identifier, Object value, Long updateTimeEpochMillis) {
        this.identifier = identifier;
        this.value = value;
        this.updateTimeEpochMillis = updateTimeEpochMillis;
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

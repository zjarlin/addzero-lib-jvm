package site.addzero.biz.spec.iot.tdengine;

import site.addzero.biz.spec.iot.IotThingRef;

import java.time.LocalDateTime;

/**
 * History query input DTO.
 */
public final class TelemetryHistoryQuery {

    private final IotThingRef schemaRef;
    private final IotThingRef sourceRef;
    private final String identifier;
    private final LocalDateTime fromTime;
    private final LocalDateTime toTime;

    public TelemetryHistoryQuery(IotThingRef schemaRef, IotThingRef sourceRef, String identifier, LocalDateTime fromTime, LocalDateTime toTime) {
        if (schemaRef == null || sourceRef == null) {
            throw new IllegalArgumentException("schemaRef and sourceRef must not be null");
        }
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("identifier must not be blank");
        }
        if (fromTime == null || toTime == null) {
            throw new IllegalArgumentException("fromTime and toTime must not be null");
        }
        this.schemaRef = schemaRef;
        this.sourceRef = sourceRef;
        this.identifier = identifier.trim();
        this.fromTime = fromTime;
        this.toTime = toTime;
    }

    public IotThingRef getSchemaRef() {
        return schemaRef;
    }

    public IotThingRef getSourceRef() {
        return sourceRef;
    }

    public String getIdentifier() {
        return identifier;
    }

    public LocalDateTime getFromTime() {
        return fromTime;
    }

    public LocalDateTime getToTime() {
        return toTime;
    }
}

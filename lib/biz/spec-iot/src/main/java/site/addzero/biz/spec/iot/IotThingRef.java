package site.addzero.biz.spec.iot;

import java.util.Objects;

/**
 * Generic thing reference used by schema and telemetry APIs.
 */
public final class IotThingRef {

    private final String kind;
    private final String id;

    private IotThingRef(String kind, String id) {
        this.kind = requireText(kind, "kind");
        this.id = requireText(id, "id");
    }

    public static IotThingRef of(String kind, String id) {
        return new IotThingRef(kind, id);
    }

    public String getKind() {
        return kind;
    }

    public String getId() {
        return id;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof IotThingRef)) {
            return false;
        }
        IotThingRef that = (IotThingRef) other;
        return kind.equals(that.kind) && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, id);
    }

    @Override
    public String toString() {
        return kind + ":" + id;
    }
}

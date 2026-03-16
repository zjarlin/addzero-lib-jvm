package site.addzero.biz.spec.iot.tdengine;

import java.util.Locale;
import java.util.Objects;

/**
 * TDengine column specification.
 */
public final class TdColumnSpec {

    public static final String TYPE_TINYINT = "TINYINT";
    public static final String TYPE_INT = "INT";
    public static final String TYPE_FLOAT = "FLOAT";
    public static final String TYPE_DOUBLE = "DOUBLE";
    public static final String TYPE_BOOL = "BOOL";
    public static final String TYPE_NCHAR = "NCHAR";
    public static final String TYPE_VARCHAR = "VARCHAR";
    public static final String TYPE_TIMESTAMP = "TIMESTAMP";
    public static final String NOTE_TAG = "TAG";

    private final String field;
    private final String type;
    private final Integer length;
    private final String note;

    public TdColumnSpec(String field, String type, Integer length, String note) {
        if (field == null || field.trim().isEmpty()) {
            throw new IllegalArgumentException("field must not be blank");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("type must not be blank");
        }
        this.field = field.trim().toLowerCase(Locale.ROOT);
        this.type = type.trim().toUpperCase(Locale.ROOT);
        this.length = length;
        this.note = note == null ? null : note.trim();
    }

    public String getField() {
        return field;
    }

    public String getType() {
        return type;
    }

    public Integer getLength() {
        return length;
    }

    public String getNote() {
        return note;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TdColumnSpec)) {
            return false;
        }
        TdColumnSpec that = (TdColumnSpec) other;
        return field.equals(that.field)
                && type.equals(that.type)
                && Objects.equals(length, that.length)
                && Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, type, length, note);
    }
}

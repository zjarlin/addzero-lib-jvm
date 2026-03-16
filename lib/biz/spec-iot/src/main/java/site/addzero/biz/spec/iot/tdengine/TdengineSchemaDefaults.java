package site.addzero.biz.spec.iot.tdengine;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Default TDengine reserved columns and tags.
 */
public final class TdengineSchemaDefaults {

    public static final String TS_COLUMN = "ts";
    public static final String REPORT_TIME_COLUMN = "report_time";
    public static final String DEVICE_ID_TAG_COLUMN = "device_id";
    public static final int DEFAULT_VARCHAR_LENGTH = 1024;
    public static final int DEFAULT_DEVICE_ID_LENGTH = 128;

    private static final Set<String> RESERVED_FIELD_NAMES = Collections.unmodifiableSet(
            new LinkedHashSet<String>(Arrays.asList(TS_COLUMN, REPORT_TIME_COLUMN, DEVICE_ID_TAG_COLUMN))
    );

    private TdengineSchemaDefaults() {
    }

    public static TdColumnSpec tsColumn() {
        return new TdColumnSpec(TS_COLUMN, TdColumnSpec.TYPE_TIMESTAMP, null, null);
    }

    public static TdColumnSpec reportTimeColumn() {
        return new TdColumnSpec(REPORT_TIME_COLUMN, TdColumnSpec.TYPE_TIMESTAMP, null, null);
    }

    public static TdColumnSpec deviceIdTagColumn() {
        return new TdColumnSpec(DEVICE_ID_TAG_COLUMN, TdColumnSpec.TYPE_NCHAR, DEFAULT_DEVICE_ID_LENGTH, TdColumnSpec.NOTE_TAG);
    }

    public static Set<String> getReservedFieldNames() {
        return RESERVED_FIELD_NAMES;
    }

    public static int getDefaultVarcharLength() {
        return DEFAULT_VARCHAR_LENGTH;
    }
}

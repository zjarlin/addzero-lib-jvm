package site.addzero.biz.spec.iot.tdengine

import java.util.LinkedHashSet

/**
 * Default TDengine reserved columns and tags.
 */
object TdengineSchemaDefaults {

    const val TS_COLUMN: String = "ts"
    const val REPORT_TIME_COLUMN: String = "report_time"
    const val DEVICE_ID_TAG_COLUMN: String = "device_id"
    const val DEFAULT_VARCHAR_LENGTH: Int = 1024
    const val DEFAULT_DEVICE_ID_LENGTH: Int = 128

    private val reservedFieldNames = linkedSetOf(
        TS_COLUMN,
        REPORT_TIME_COLUMN,
        DEVICE_ID_TAG_COLUMN,
    ).toSet()

    @JvmStatic
    fun tsColumn(): TdColumnSpec {
        return TdColumnSpec(TS_COLUMN, TdColumnSpec.TYPE_TIMESTAMP, null, null)
    }

    @JvmStatic
    fun reportTimeColumn(): TdColumnSpec {
        return TdColumnSpec(REPORT_TIME_COLUMN, TdColumnSpec.TYPE_TIMESTAMP, null, null)
    }

    @JvmStatic
    fun deviceIdTagColumn(): TdColumnSpec {
        return TdColumnSpec(
            DEVICE_ID_TAG_COLUMN,
            TdColumnSpec.TYPE_NCHAR,
            DEFAULT_DEVICE_ID_LENGTH,
            TdColumnSpec.NOTE_TAG,
        )
    }

    @JvmStatic
    fun getReservedFieldNames(): Set<String> {
        return LinkedHashSet(reservedFieldNames).toSet()
    }

    @JvmStatic
    fun getDefaultVarcharLength(): Int {
        return DEFAULT_VARCHAR_LENGTH
    }
}

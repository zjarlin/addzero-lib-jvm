package site.addzero.biz.spec.iot.tdengine

import site.addzero.biz.spec.iot.requireText
import site.addzero.biz.spec.iot.trimToNull
import java.util.Locale

/**
 * TDengine column specification.
 */
class TdColumnSpec(
    field: String?,
    type: String?,
    val length: Int?,
    note: String?,
) {

    val field: String = requireText(field, "field").lowercase(Locale.ROOT)
    val type: String = requireText(type, "type").uppercase(Locale.ROOT)
    val note: String? = trimToNull(note)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is TdColumnSpec) {
            return false
        }
        return field == other.field &&
            type == other.type &&
            length == other.length &&
            note == other.note
    }

    override fun hashCode(): Int {
        var result = field.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (length?.hashCode() ?: 0)
        result = 31 * result + (note?.hashCode() ?: 0)
        return result
    }

    companion object {
        @JvmField
        val TYPE_TINYINT: String = "TINYINT"

        @JvmField
        val TYPE_INT: String = "INT"

        @JvmField
        val TYPE_FLOAT: String = "FLOAT"

        @JvmField
        val TYPE_DOUBLE: String = "DOUBLE"

        @JvmField
        val TYPE_BOOL: String = "BOOL"

        @JvmField
        val TYPE_NCHAR: String = "NCHAR"

        @JvmField
        val TYPE_VARCHAR: String = "VARCHAR"

        @JvmField
        val TYPE_TIMESTAMP: String = "TIMESTAMP"

        @JvmField
        val NOTE_TAG: String = "TAG"
    }
}

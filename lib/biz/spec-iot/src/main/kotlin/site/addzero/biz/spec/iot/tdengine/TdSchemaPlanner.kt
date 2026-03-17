package site.addzero.biz.spec.iot.tdengine

import java.util.LinkedHashMap
import java.util.LinkedHashSet

/**
 * TDengine schema diff planner extracted from MyBatis mapper logic.
 */
class TdSchemaPlanner @JvmOverloads constructor(
    reservedFieldNames: Set<String> = TdengineSchemaDefaults.getReservedFieldNames(),
) {

    private val reservedFieldNames: Set<String> = LinkedHashSet(reservedFieldNames)

    fun plan(existingColumns: List<TdColumnSpec>, desiredColumns: List<TdColumnSpec>): TdSchemaDiff {
        val currentByField = indexByField(filterReserved(existingColumns))
        val desiredByField = indexByField(desiredColumns)

        val addedColumns = mutableListOf<TdColumnSpec>()
        val droppedColumns = mutableListOf<TdColumnSpec>()
        val modifiedColumns = mutableListOf<TdColumnSpec>()
        val recreatedColumns = mutableListOf<TdColumnSpec>()

        desiredColumns.forEach { desired ->
            val existing = currentByField[desired.field]
            if (existing == null) {
                addedColumns += desired
                return@forEach
            }
            if (existing.type != desired.type) {
                recreatedColumns += desired
                return@forEach
            }
            if (desired.length != null) {
                val existingLength = existing.length
                if (existingLength == null || desired.length > existingLength) {
                    modifiedColumns += desired
                } else if (desired.length < existingLength) {
                    recreatedColumns += desired
                }
            }
        }

        currentByField.values.forEach { existing ->
            if (!desiredByField.containsKey(existing.field)) {
                droppedColumns += existing
            }
        }

        return TdSchemaDiff(
            addedColumns = addedColumns,
            droppedColumns = droppedColumns,
            modifiedColumns = modifiedColumns,
            recreatedColumns = recreatedColumns,
        )
    }

    private fun filterReserved(columns: List<TdColumnSpec>): List<TdColumnSpec> {
        return columns.filterNot { reservedFieldNames.contains(it.field) }
    }

    private fun indexByField(columns: List<TdColumnSpec>): Map<String, TdColumnSpec> {
        val indexed = LinkedHashMap<String, TdColumnSpec>()
        columns.forEach { column ->
            indexed[column.field] = column
        }
        return indexed
    }
}

package site.addzero.biz.spec.iot.tdengine

/**
 * Result of TDengine schema comparison.
 */
class TdSchemaDiff(
    addedColumns: List<TdColumnSpec>,
    droppedColumns: List<TdColumnSpec>,
    modifiedColumns: List<TdColumnSpec>,
    recreatedColumns: List<TdColumnSpec>,
) {

    val addedColumns: List<TdColumnSpec> = ArrayList(addedColumns).toList()
    val droppedColumns: List<TdColumnSpec> = ArrayList(droppedColumns).toList()
    val modifiedColumns: List<TdColumnSpec> = ArrayList(modifiedColumns).toList()
    val recreatedColumns: List<TdColumnSpec> = ArrayList(recreatedColumns).toList()

    fun isEmpty(): Boolean {
        return addedColumns.isEmpty() &&
            droppedColumns.isEmpty() &&
            modifiedColumns.isEmpty() &&
            recreatedColumns.isEmpty()
    }
}

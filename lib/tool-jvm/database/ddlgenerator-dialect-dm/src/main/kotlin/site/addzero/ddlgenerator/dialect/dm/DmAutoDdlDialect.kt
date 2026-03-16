package site.addzero.ddlgenerator.dialect.dm

import site.addzero.ddlgenerator.core.dialect.AbstractSqlDialect
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.util.db.DatabaseType

class DmAutoDdlDialect : AbstractSqlDialect(DatabaseType.DM) {

    override fun supportsInlinePrimaryKey(column: AutoDdlColumn): Boolean {
        return column.primaryKey
    }

    override fun renderAutoIncrementClause(column: AutoDdlColumn): String? {
        return if (column.autoIncrement) "IDENTITY(1, 1)" else null
    }

    override fun columnType(column: AutoDdlColumn): String {
        return when (column.logicalType) {
            AutoDdlLogicalType.STRING -> "VARCHAR(${column.length ?: 255})"
            AutoDdlLogicalType.TEXT -> "CLOB"
            AutoDdlLogicalType.CHAR -> "CHAR(${column.length ?: 1})"
            AutoDdlLogicalType.BOOLEAN -> "BIT"
            AutoDdlLogicalType.INT8 -> "TINYINT"
            AutoDdlLogicalType.INT16 -> "SMALLINT"
            AutoDdlLogicalType.INT32 -> "INT"
            AutoDdlLogicalType.INT64 -> "BIGINT"
            AutoDdlLogicalType.DECIMAL -> "DECIMAL(${column.precision ?: 19}, ${column.scale ?: 2})"
            AutoDdlLogicalType.BIG_INTEGER -> "DECIMAL(65, 0)"
            AutoDdlLogicalType.FLOAT32 -> "REAL"
            AutoDdlLogicalType.FLOAT64 -> "DOUBLE"
            AutoDdlLogicalType.DATE -> "DATE"
            AutoDdlLogicalType.TIME -> "TIME"
            AutoDdlLogicalType.DATETIME,
            AutoDdlLogicalType.TIMESTAMP,
            AutoDdlLogicalType.DATETIME_TZ -> "TIMESTAMP"
            AutoDdlLogicalType.DURATION -> "BIGINT"
            AutoDdlLogicalType.BINARY -> "BLOB"
            AutoDdlLogicalType.UUID -> "VARCHAR(36)"
            AutoDdlLogicalType.JSON -> "CLOB"
            AutoDdlLogicalType.UNKNOWN -> column.nativeTypeHint ?: "VARCHAR(255)"
        }
    }
}

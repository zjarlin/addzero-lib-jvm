package site.addzero.ddlgenerator.dialect.h2

import site.addzero.ddlgenerator.core.dialect.AbstractSqlDialect
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.util.db.DatabaseType

class H2AutoDdlDialect : AbstractSqlDialect(DatabaseType.H2) {

    override fun supportsInlinePrimaryKey(column: AutoDdlColumn): Boolean {
        return column.primaryKey
    }

    override fun renderAutoIncrementClause(column: AutoDdlColumn): String? {
        return if (column.autoIncrement) "AUTO_INCREMENT" else null
    }

    override fun columnType(column: AutoDdlColumn): String {
        return when (column.logicalType) {
            AutoDdlLogicalType.TEXT -> "CLOB"
            AutoDdlLogicalType.UUID -> "UUID"
            AutoDdlLogicalType.JSON -> "JSON"
            AutoDdlLogicalType.BINARY -> "BLOB"
            else -> super.columnType(column)
        }
    }
}

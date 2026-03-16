package site.addzero.ddlgenerator.dialect.mysql

import site.addzero.ddlgenerator.core.dialect.AbstractSqlDialect
import site.addzero.ddlgenerator.core.dialect.AutoDdlIdentifierQuote
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlComment
import site.addzero.ddlgenerator.core.model.AutoDdlCommentTargetType
import site.addzero.ddlgenerator.core.model.AutoDdlIndex
import site.addzero.ddlgenerator.core.model.AutoDdlIndexType
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.util.db.DatabaseType

class MySqlAutoDdlDialect : AbstractSqlDialect(
    databaseType = DatabaseType.MYSQL,
    quote = AutoDdlIdentifierQuote("`", "`"),
) {

    override fun supportsInlinePrimaryKey(column: AutoDdlColumn): Boolean {
        return column.primaryKey
    }

    override fun renderAutoIncrementClause(column: AutoDdlColumn): String? {
        return if (column.autoIncrement) "AUTO_INCREMENT" else null
    }

    override fun renderDropIndex(tableName: String, indexName: String): String {
        return "DROP INDEX ${quoteIdentifier(indexName)} ON ${quoteIdentifier(tableName)}"
    }

    override fun renderTableComment(tableName: String, comment: String): List<String> {
        return listOf("ALTER TABLE ${quoteIdentifier(tableName)} COMMENT = '${comment.replace("'", "''")}'")
    }

    override fun renderColumnComment(tableName: String, columnName: String, comment: String): List<String> {
        return emptyList()
    }

    override fun renderCreateIndex(tableName: String, index: AutoDdlIndex): String {
        val keyword = when (index.type) {
            AutoDdlIndexType.UNIQUE -> "CREATE UNIQUE INDEX"
            AutoDdlIndexType.FULLTEXT -> "CREATE FULLTEXT INDEX"
            AutoDdlIndexType.NORMAL -> "CREATE INDEX"
        }
        val columns = index.columnNames.joinToString(", ") { quoteIdentifier(it) }
        return "$keyword ${quoteIdentifier(index.name)} ON ${quoteIdentifier(tableName)} ($columns)"
    }

    override fun columnType(column: AutoDdlColumn): String {
        return when (column.logicalType) {
            AutoDdlLogicalType.STRING -> "VARCHAR(${column.length ?: 255})"
            AutoDdlLogicalType.TEXT -> "LONGTEXT"
            AutoDdlLogicalType.CHAR -> "CHAR(${column.length ?: 1})"
            AutoDdlLogicalType.BOOLEAN -> "BIT"
            AutoDdlLogicalType.INT8 -> "TINYINT"
            AutoDdlLogicalType.INT16 -> "SMALLINT"
            AutoDdlLogicalType.INT32 -> "INT"
            AutoDdlLogicalType.INT64 -> "BIGINT"
            AutoDdlLogicalType.DECIMAL -> "DECIMAL(${column.precision ?: 19}, ${column.scale ?: 2})"
            AutoDdlLogicalType.BIG_INTEGER -> "DECIMAL(65, 0)"
            AutoDdlLogicalType.FLOAT32 -> "FLOAT"
            AutoDdlLogicalType.FLOAT64 -> "DOUBLE"
            AutoDdlLogicalType.DATE -> "DATE"
            AutoDdlLogicalType.TIME -> "TIME"
            AutoDdlLogicalType.DATETIME, AutoDdlLogicalType.TIMESTAMP -> "DATETIME"
            AutoDdlLogicalType.DATETIME_TZ -> "TIMESTAMP"
            AutoDdlLogicalType.DURATION -> "BIGINT"
            AutoDdlLogicalType.BINARY -> "LONGBLOB"
            AutoDdlLogicalType.UUID -> "VARCHAR(36)"
            AutoDdlLogicalType.JSON -> "JSON"
            AutoDdlLogicalType.UNKNOWN -> column.nativeTypeHint ?: "VARCHAR(255)"
        }
    }
}

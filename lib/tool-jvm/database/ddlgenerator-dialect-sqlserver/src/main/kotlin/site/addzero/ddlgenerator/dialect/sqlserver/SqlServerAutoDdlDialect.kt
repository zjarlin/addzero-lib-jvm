package site.addzero.ddlgenerator.dialect.sqlserver

import site.addzero.ddlgenerator.core.dialect.AbstractSqlDialect
import site.addzero.ddlgenerator.core.dialect.AutoDdlIdentifierQuote
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlComment
import site.addzero.ddlgenerator.core.model.AutoDdlCommentTargetType
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.util.db.DatabaseType

class SqlServerAutoDdlDialect : AbstractSqlDialect(
    databaseType = DatabaseType.SQLSERVER,
    quote = AutoDdlIdentifierQuote("[", "]"),
) {

    override fun supportsInlinePrimaryKey(column: AutoDdlColumn): Boolean {
        return column.primaryKey
    }

    override fun renderAutoIncrementClause(column: AutoDdlColumn): String? {
        return if (column.autoIncrement) "IDENTITY(1,1)" else null
    }

    override fun renderDropIndex(tableName: String, indexName: String): String {
        return "DROP INDEX ${quoteIdentifier(indexName)} ON ${quoteIdentifier(tableName)}"
    }

    override fun renderComment(comment: AutoDdlComment): List<String> {
        return when (comment.targetType) {
            AutoDdlCommentTargetType.TABLE -> listOf(
                "EXEC sp_addextendedproperty @name = N'MS_Description', @value = N'${comment.value}', @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'${comment.tableName}'"
            )
            AutoDdlCommentTargetType.COLUMN -> listOf(
                "EXEC sp_addextendedproperty @name = N'MS_Description', @value = N'${comment.value}', @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'${comment.tableName}', @level2type = N'COLUMN', @level2name = N'${comment.columnName}'"
            )
            AutoDdlCommentTargetType.SEQUENCE -> emptyList()
        }
    }

    override fun columnType(column: AutoDdlColumn): String {
        return when (column.logicalType) {
            AutoDdlLogicalType.STRING -> "NVARCHAR(${column.length ?: 255})"
            AutoDdlLogicalType.TEXT -> "NVARCHAR(MAX)"
            AutoDdlLogicalType.CHAR -> "NCHAR(${column.length ?: 1})"
            AutoDdlLogicalType.BOOLEAN -> "BIT"
            AutoDdlLogicalType.INT8 -> "TINYINT"
            AutoDdlLogicalType.INT16 -> "SMALLINT"
            AutoDdlLogicalType.INT32 -> "INT"
            AutoDdlLogicalType.INT64 -> "BIGINT"
            AutoDdlLogicalType.DECIMAL -> "DECIMAL(${column.precision ?: 19}, ${column.scale ?: 2})"
            AutoDdlLogicalType.BIG_INTEGER -> "DECIMAL(65, 0)"
            AutoDdlLogicalType.FLOAT32 -> "REAL"
            AutoDdlLogicalType.FLOAT64 -> "FLOAT"
            AutoDdlLogicalType.DATE -> "DATE"
            AutoDdlLogicalType.TIME -> "TIME"
            AutoDdlLogicalType.DATETIME, AutoDdlLogicalType.TIMESTAMP -> "DATETIME2"
            AutoDdlLogicalType.DATETIME_TZ -> "DATETIMEOFFSET"
            AutoDdlLogicalType.DURATION -> "BIGINT"
            AutoDdlLogicalType.BINARY -> "VARBINARY(MAX)"
            AutoDdlLogicalType.UUID -> "UNIQUEIDENTIFIER"
            AutoDdlLogicalType.JSON -> "NVARCHAR(MAX)"
            AutoDdlLogicalType.UNKNOWN -> column.nativeTypeHint ?: "NVARCHAR(255)"
        }
    }
}

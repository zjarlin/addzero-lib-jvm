package site.addzero.ddlgenerator.core.dialect

import site.addzero.ddlgenerator.core.diff.AddColumn
import site.addzero.ddlgenerator.core.diff.AddComment
import site.addzero.ddlgenerator.core.diff.AddForeignKey
import site.addzero.ddlgenerator.core.diff.AlterColumn
import site.addzero.ddlgenerator.core.diff.AutoDdlOperation
import site.addzero.ddlgenerator.core.diff.CreateIndex
import site.addzero.ddlgenerator.core.diff.CreateSequence
import site.addzero.ddlgenerator.core.diff.CreateTable
import site.addzero.ddlgenerator.core.diff.DropColumn
import site.addzero.ddlgenerator.core.diff.DropForeignKey
import site.addzero.ddlgenerator.core.diff.DropIndex
import site.addzero.ddlgenerator.core.diff.DropTable
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlComment
import site.addzero.ddlgenerator.core.model.AutoDdlCommentTargetType
import site.addzero.ddlgenerator.core.model.AutoDdlIndexType
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.ddlgenerator.core.model.AutoDdlTable
import site.addzero.util.db.DatabaseType
import java.util.ServiceLoader

data class AutoDdlIdentifierQuote(
    val prefix: String = "\"",
    val suffix: String = prefix,
)

data class AutoDdlRenderContext(
    val appendSemicolon: Boolean = true,
)

interface AutoDdlDialect {
    val databaseType: DatabaseType

    fun supports(type: DatabaseType): Boolean {
        return databaseType == type
    }

    fun render(
        operation: AutoDdlOperation,
        context: AutoDdlRenderContext = AutoDdlRenderContext(),
    ): List<String>

    fun render(
        operations: List<AutoDdlOperation>,
        context: AutoDdlRenderContext = AutoDdlRenderContext(),
    ): List<String> {
        return operations.flatMap { render(it, context) }.filter { it.isNotBlank() }
    }
}

object AutoDdlDialects {
    fun load(classLoader: ClassLoader = AutoDdlDialect::class.java.classLoader): List<AutoDdlDialect> {
        return ServiceLoader.load(AutoDdlDialect::class.java, classLoader).toList()
    }

    fun resolve(
        databaseType: DatabaseType,
        classLoader: ClassLoader = AutoDdlDialect::class.java.classLoader,
    ): AutoDdlDialect? {
        return load(classLoader).firstOrNull { it.supports(databaseType) }
    }

    fun require(
        databaseType: DatabaseType,
        classLoader: ClassLoader = AutoDdlDialect::class.java.classLoader,
    ): AutoDdlDialect {
        return resolve(databaseType, classLoader)
            ?: error("No AutoDDL dialect registered for $databaseType")
    }
}

abstract class AbstractSqlDialect(
    final override val databaseType: DatabaseType,
    private val quote: AutoDdlIdentifierQuote = AutoDdlIdentifierQuote(),
) : AutoDdlDialect {

    override fun render(
        operation: AutoDdlOperation,
        context: AutoDdlRenderContext,
    ): List<String> {
        return when (operation) {
            is CreateSequence -> listOf(withTerminator(renderCreateSequence(operation), context))
            is CreateTable -> listOf(withTerminator(renderCreateTable(operation.table), context))
            is DropTable -> listOf(withTerminator(renderDropTable(operation.tableName), context))
            is AddColumn -> listOf(withTerminator(renderAddColumn(operation.tableName, operation.column), context))
            is AlterColumn -> renderAlterColumn(operation.tableName, operation.column).map { withTerminator(it, context) }
            is DropColumn -> listOf(withTerminator(renderDropColumn(operation.tableName, operation.columnName), context))
            is CreateIndex -> listOf(withTerminator(renderCreateIndex(operation.tableName, operation.index), context))
            is DropIndex -> listOf(withTerminator(renderDropIndex(operation.tableName, operation.indexName), context))
            is AddForeignKey -> renderAddForeignKey(operation.tableName, operation.foreignKey).map { withTerminator(it, context) }
            is DropForeignKey -> renderDropForeignKey(operation.tableName, operation.foreignKeyName).map { withTerminator(it, context) }
            is AddComment -> renderComment(operation.comment).map { withTerminator(it, context) }
        }
    }

    protected fun quoteIdentifier(value: String): String {
        if (quote.prefix.isEmpty() && quote.suffix.isEmpty()) {
            return value
        }
        return "${quote.prefix}$value${quote.suffix}"
    }

    protected open fun renderCreateSequence(operation: CreateSequence): String {
        return "CREATE SEQUENCE ${quoteIdentifier(operation.sequence.name)} START WITH ${operation.sequence.startWith} INCREMENT BY ${operation.sequence.incrementBy}"
    }

    protected open fun renderCreateTable(table: AutoDdlTable): String {
        val body = buildList {
            addAll(table.columns.map { renderColumnDefinition(it) })
            if (table.primaryKeyColumnNames.size > 1) {
                add("PRIMARY KEY (${table.primaryKeyColumnNames.joinToString(", ") { quoteIdentifier(it) }})")
            }
        }.joinToString(",\n  ")
        return """
            CREATE TABLE ${quoteIdentifier(table.name)} (
              $body
            )
        """.trimIndent()
    }

    protected open fun renderDropTable(tableName: String): String {
        return "DROP TABLE IF EXISTS ${quoteIdentifier(tableName)}"
    }

    protected open fun renderAddColumn(tableName: String, column: AutoDdlColumn): String {
        return "ALTER TABLE ${quoteIdentifier(tableName)} ADD COLUMN ${renderColumnDefinition(column)}"
    }

    protected open fun renderAlterColumn(tableName: String, column: AutoDdlColumn): List<String> {
        return listOf("ALTER TABLE ${quoteIdentifier(tableName)} ALTER COLUMN ${renderColumnDefinition(column)}")
    }

    protected open fun renderDropColumn(tableName: String, columnName: String): String {
        return "ALTER TABLE ${quoteIdentifier(tableName)} DROP COLUMN ${quoteIdentifier(columnName)}"
    }

    protected open fun renderCreateIndex(tableName: String, index: site.addzero.ddlgenerator.core.model.AutoDdlIndex): String {
        val indexKeyword = when (index.type) {
            AutoDdlIndexType.UNIQUE -> "UNIQUE INDEX"
            AutoDdlIndexType.FULLTEXT -> "FULLTEXT INDEX"
            AutoDdlIndexType.NORMAL -> "INDEX"
        }
        val columns = index.columnNames.joinToString(", ") { quoteIdentifier(it) }
        return "CREATE $indexKeyword ${quoteIdentifier(index.name)} ON ${quoteIdentifier(tableName)} ($columns)"
    }

    protected open fun renderDropIndex(tableName: String, indexName: String): String {
        return "DROP INDEX ${quoteIdentifier(indexName)}"
    }

    protected open fun renderAddForeignKey(
        tableName: String,
        foreignKey: site.addzero.ddlgenerator.core.model.AutoDdlForeignKey,
    ): List<String> {
        val columns = foreignKey.columnNames.joinToString(", ") { quoteIdentifier(it) }
        val referencedColumns = foreignKey.referencedColumnNames.joinToString(", ") { quoteIdentifier(it) }
        val builder = StringBuilder()
        builder.append("ALTER TABLE ${quoteIdentifier(tableName)} ADD CONSTRAINT ${quoteIdentifier(foreignKey.name)} ")
        builder.append("FOREIGN KEY ($columns) REFERENCES ${quoteIdentifier(foreignKey.referencedTableName)} ($referencedColumns)")
        foreignKey.onDelete?.takeIf { it.isNotBlank() }?.let { builder.append(" ON DELETE $it") }
        foreignKey.onUpdate?.takeIf { it.isNotBlank() }?.let { builder.append(" ON UPDATE $it") }
        return listOf(builder.toString())
    }

    protected open fun renderDropForeignKey(tableName: String, foreignKeyName: String): List<String> {
        return listOf("ALTER TABLE ${quoteIdentifier(tableName)} DROP CONSTRAINT ${quoteIdentifier(foreignKeyName)}")
    }

    protected open fun renderComment(comment: AutoDdlComment): List<String> {
        return when (comment.targetType) {
            AutoDdlCommentTargetType.TABLE -> renderTableComment(comment.tableName.orEmpty(), comment.value)
            AutoDdlCommentTargetType.COLUMN -> renderColumnComment(comment.tableName.orEmpty(), comment.columnName.orEmpty(), comment.value)
            AutoDdlCommentTargetType.SEQUENCE -> renderSequenceComment(comment.sequenceName.orEmpty(), comment.value)
        }
    }

    protected open fun renderTableComment(tableName: String, comment: String): List<String> {
        return listOf("COMMENT ON TABLE ${quoteIdentifier(tableName)} IS '${comment.escapeSqlLiteral()}'")
    }

    protected open fun renderColumnComment(tableName: String, columnName: String, comment: String): List<String> {
        return listOf("COMMENT ON COLUMN ${quoteIdentifier(tableName)}.${quoteIdentifier(columnName)} IS '${comment.escapeSqlLiteral()}'")
    }

    protected open fun renderSequenceComment(sequenceName: String, comment: String): List<String> {
        return emptyList()
    }

    protected open fun renderColumnDefinition(column: AutoDdlColumn): String {
        val parts = mutableListOf<String>()
        parts += quoteIdentifier(column.name)
        parts += columnType(column)
        if (column.autoIncrement) {
            renderAutoIncrementClause(column)?.let { parts += it }
        }
        if (!column.nullable) {
            parts += "NOT NULL"
        }
        column.defaultValue?.takeIf { it.isNotBlank() }?.let { parts += "DEFAULT $it" }
        if (column.primaryKey && supportsInlinePrimaryKey(column)) {
            parts += "PRIMARY KEY"
        }
        return parts.joinToString(" ")
    }

    protected open fun supportsInlinePrimaryKey(column: AutoDdlColumn): Boolean {
        return false
    }

    protected open fun renderAutoIncrementClause(column: AutoDdlColumn): String? {
        return null
    }

    protected open fun columnType(column: AutoDdlColumn): String {
        return when (column.logicalType) {
            AutoDdlLogicalType.STRING -> "VARCHAR(${column.length ?: 255})"
            AutoDdlLogicalType.TEXT -> "TEXT"
            AutoDdlLogicalType.CHAR -> "CHAR(${column.length ?: 1})"
            AutoDdlLogicalType.BOOLEAN -> "BOOLEAN"
            AutoDdlLogicalType.INT8 -> "TINYINT"
            AutoDdlLogicalType.INT16 -> "SMALLINT"
            AutoDdlLogicalType.INT32 -> "INT"
            AutoDdlLogicalType.INT64 -> "BIGINT"
            AutoDdlLogicalType.DECIMAL -> {
                val precision = column.precision ?: 19
                val scale = column.scale ?: 2
                "DECIMAL($precision, $scale)"
            }
            AutoDdlLogicalType.BIG_INTEGER -> "DECIMAL(65, 0)"
            AutoDdlLogicalType.FLOAT32 -> "REAL"
            AutoDdlLogicalType.FLOAT64 -> "DOUBLE"
            AutoDdlLogicalType.DATE -> "DATE"
            AutoDdlLogicalType.TIME -> "TIME"
            AutoDdlLogicalType.DATETIME -> "TIMESTAMP"
            AutoDdlLogicalType.DATETIME_TZ -> "TIMESTAMP WITH TIME ZONE"
            AutoDdlLogicalType.TIMESTAMP -> "TIMESTAMP"
            AutoDdlLogicalType.DURATION -> "BIGINT"
            AutoDdlLogicalType.BINARY -> "BLOB"
            AutoDdlLogicalType.UUID -> "VARCHAR(36)"
            AutoDdlLogicalType.JSON -> "JSON"
            AutoDdlLogicalType.UNKNOWN -> column.nativeTypeHint ?: "VARCHAR(255)"
        }
    }

    private fun withTerminator(statement: String, context: AutoDdlRenderContext): String {
        if (!context.appendSemicolon || statement.endsWith(";")) {
            return statement
        }
        return "$statement;"
    }

    private fun String.escapeSqlLiteral(): String {
        return replace("'", "''")
    }
}

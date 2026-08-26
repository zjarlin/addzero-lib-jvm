package site.addzero.ddlgenerator.dialect.mysql

import site.addzero.ddlgenerator.core.dialect.AbstractSqlDialect
import site.addzero.ddlgenerator.core.dialect.AutoDdlIdentifierQuote
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlComment
import site.addzero.ddlgenerator.core.model.AutoDdlCommentTargetType
import site.addzero.ddlgenerator.core.model.AutoDdlIndex
import site.addzero.ddlgenerator.core.model.AutoDdlIndexType
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.ddlgenerator.core.model.AutoDdlTable
import site.addzero.util.db.DatabaseType

class MySqlAutoDdlDialect : AbstractSqlDialect(
    databaseType = DatabaseType.MYSQL,
    quote = AutoDdlIdentifierQuote("`", "`"),
) {

    override fun supportsInlinePrimaryKey(column: AutoDdlColumn): Boolean {
        return column.primaryKey
    }

    override fun renderCreateTable(table: AutoDdlTable): String {
        val primaryKeyColumns = table.columns.filter { it.primaryKey }
        val body = buildList {
            addAll(table.columns.map { renderColumnDefinition(it) })
            if (
                primaryKeyColumns.size > 1 ||
                (primaryKeyColumns.size == 1 && !supportsInlinePrimaryKey(primaryKeyColumns.single()))
            ) {
                add("PRIMARY KEY (${table.primaryKeyColumnNames.joinToString(", ") { quoteIdentifier(it) }})")
            }
        }.joinToString(",\n")
        return buildString {
            append("CREATE TABLE IF NOT EXISTS ${quoteIdentifier(table.name)} (\n")
            append(body.prependIndent("  "))
            append("\n)")
        }
    }

    override fun renderAutoIncrementClause(column: AutoDdlColumn): String? {
        return if (column.autoIncrement) "AUTO_INCREMENT" else null
    }

    override fun renderDropIndex(tableName: String, indexName: String): String {
        return "DROP INDEX ${quoteIdentifier(indexName)} ON ${quoteIdentifier(tableName)}"
    }

    override fun renderRenameTable(
        oldTableName: String,
        newTableName: String,
    ): List<String> {
        return listOf(
            """
            SET @ddl = (
              SELECT IF(
                EXISTS (
                  SELECT 1 FROM information_schema.TABLES
                  WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = '${oldTableName.replace("'", "''")}'
                )
                AND NOT EXISTS (
                  SELECT 1 FROM information_schema.TABLES
                  WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = '${newTableName.replace("'", "''")}'
                ),
                'RENAME TABLE ${quoteIdentifier(oldTableName)} TO ${quoteIdentifier(newTableName)}',
                'SELECT 1'
              )
            )
            """.trimIndent(),
            "PREPARE stmt FROM @ddl",
            "EXECUTE stmt",
            "DEALLOCATE PREPARE stmt",
        )
    }

    override fun renderDropColumnNotNull(tableName: String, columnName: String): List<String> {
        return listOf(
            """
            SET @ddl = (
              SELECT CONCAT('ALTER TABLE ${quoteIdentifier(tableName)} MODIFY COLUMN ${quoteIdentifier(columnName)} ', COLUMN_TYPE, ' NULL')
              FROM information_schema.COLUMNS
              WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_NAME = '${tableName.replace("'", "''")}'
                AND COLUMN_NAME = '${columnName.replace("'", "''")}'
            )
            """.trimIndent(),
            "PREPARE stmt FROM @ddl",
            "EXECUTE stmt",
            "DEALLOCATE PREPARE stmt",
        )
    }

    override fun renderSetColumnNotNull(tableName: String, column: AutoDdlColumn): List<String> {
        val table = quoteIdentifier(tableName)
        val columnName = quoteIdentifier(column.name)
        val fillValue = column.defaultValue?.takeIf { it.isNotBlank() } ?: column.fallbackDefaultValue()
        return listOf(
            "UPDATE $table SET $columnName = $fillValue WHERE $columnName IS NULL",
            """
            SET @ddl = (
              SELECT CONCAT('ALTER TABLE ${quoteIdentifier(tableName)} MODIFY COLUMN ${quoteIdentifier(column.name)} ', COLUMN_TYPE, ' NOT NULL')
              FROM information_schema.COLUMNS
              WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_NAME = '${tableName.replace("'", "''")}'
                AND COLUMN_NAME = '${column.name.replace("'", "''")}'
            )
            """.trimIndent(),
            "PREPARE stmt FROM @ddl",
            "EXECUTE stmt",
            "DEALLOCATE PREPARE stmt",
        )
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

    private fun AutoDdlColumn.fallbackDefaultValue(): String {
        return when (logicalType) {
            AutoDdlLogicalType.STRING,
            AutoDdlLogicalType.TEXT,
            AutoDdlLogicalType.CHAR,
            AutoDdlLogicalType.UUID,
            AutoDdlLogicalType.UNKNOWN -> "''"
            AutoDdlLogicalType.BOOLEAN -> "FALSE"
            AutoDdlLogicalType.INT8,
            AutoDdlLogicalType.INT16,
            AutoDdlLogicalType.INT32,
            AutoDdlLogicalType.INT64,
            AutoDdlLogicalType.DECIMAL,
            AutoDdlLogicalType.BIG_INTEGER,
            AutoDdlLogicalType.FLOAT32,
            AutoDdlLogicalType.FLOAT64,
            AutoDdlLogicalType.DURATION -> "0"
            AutoDdlLogicalType.DATE -> "CURRENT_DATE"
            AutoDdlLogicalType.TIME -> "CURRENT_TIME"
            AutoDdlLogicalType.DATETIME,
            AutoDdlLogicalType.DATETIME_TZ,
            AutoDdlLogicalType.TIMESTAMP -> "CURRENT_TIMESTAMP"
            AutoDdlLogicalType.BINARY -> "X''"
            AutoDdlLogicalType.JSON -> "'{}'"
        }
    }
}

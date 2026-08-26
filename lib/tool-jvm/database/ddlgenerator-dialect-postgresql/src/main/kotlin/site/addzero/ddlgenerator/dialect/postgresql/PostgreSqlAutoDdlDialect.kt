package site.addzero.ddlgenerator.dialect.postgresql

import site.addzero.ddlgenerator.core.dialect.AbstractSqlDialect
import site.addzero.ddlgenerator.core.model.AutoDdlIndex
import site.addzero.ddlgenerator.core.model.AutoDdlIndexType
import site.addzero.ddlgenerator.core.model.AutoDdlColumn
import site.addzero.ddlgenerator.core.model.AutoDdlLogicalType
import site.addzero.ddlgenerator.core.model.AutoDdlSchema
import site.addzero.ddlgenerator.core.model.AutoDdlForeignKey
import site.addzero.ddlgenerator.core.model.AutoDdlTable
import site.addzero.util.db.DatabaseType

class PostgreSqlAutoDdlDialect : AbstractSqlDialect(DatabaseType.POSTGRESQL) {

    override fun normalizeSchema(schema: AutoDdlSchema): AutoDdlSchema {
        return schema.mapColumns { column ->
            when (column.logicalType) {
                AutoDdlLogicalType.STRING -> {
                    if (column.length == null) column.copy(logicalType = AutoDdlLogicalType.TEXT) else column
                }

                AutoDdlLogicalType.CHAR -> column.copy(length = column.length ?: 1)
                AutoDdlLogicalType.DECIMAL -> column.copy(
                    precision = column.precision ?: 19,
                    scale = column.scale ?: 2,
                )

                AutoDdlLogicalType.BIG_INTEGER -> column.copy(
                    precision = column.precision ?: 65,
                    scale = column.scale ?: 0,
                )

                else -> column
            }
        }
    }

    override fun normalizePreviousSchema(schema: AutoDdlSchema): AutoDdlSchema {
        return schema.mapColumns { column ->
            if (column.logicalType == AutoDdlLogicalType.STRING && column.length == null) {
                column.copy(length = 255)
            } else {
                column
            }
        }
    }

    private fun AutoDdlSchema.mapColumns(transform: (AutoDdlColumn) -> AutoDdlColumn): AutoDdlSchema {
        return copy(
            tables = tables.map { table ->
                table.copy(
                    columns = table.columns.map(transform)
                )
            }
        )
    }

    override fun renderCreateSequence(operation: site.addzero.ddlgenerator.core.diff.CreateSequence): String {
        return "CREATE SEQUENCE IF NOT EXISTS ${quoteIdentifier(operation.sequence.name)} START WITH ${operation.sequence.startWith} INCREMENT BY ${operation.sequence.incrementBy}"
    }

    override fun renderCreateTable(table: AutoDdlTable): String {
        val primaryKeyColumns = table.columns.filter { it.primaryKey }
        val body = buildList {
            addAll(table.columns.map { renderColumnDefinition(it) })
            if (primaryKeyColumns.isNotEmpty()) {
                add("PRIMARY KEY (${table.primaryKeyColumnNames.joinToString(", ") { quoteIdentifier(it) }})")
            }
        }.joinToString(",\n")
        return buildString {
            append("CREATE TABLE IF NOT EXISTS ${quoteIdentifier(table.name)} (\n")
            append(body.prependIndent("  "))
            append("\n)")
        }
    }

    override fun renderAddColumn(tableName: String, column: AutoDdlColumn): List<String> {
        val table = quoteIdentifier(tableName)
        val columnName = quoteIdentifier(column.name)
        if (column.nullable || column.autoIncrement) {
            return listOf("ALTER TABLE $table ADD COLUMN IF NOT EXISTS ${renderColumnDefinition(column)}")
        }

        val type = columnType(column)
        val fillValue = column.defaultValue?.takeIf { it.isNotBlank() } ?: column.fallbackDefaultValue()
        return listOf(
            "ALTER TABLE $table ADD COLUMN IF NOT EXISTS $columnName $type DEFAULT $fillValue",
            "UPDATE $table SET $columnName = $fillValue WHERE $columnName IS NULL",
            "ALTER TABLE $table ALTER COLUMN $columnName SET NOT NULL",
            "ALTER TABLE $table ALTER COLUMN $columnName DROP DEFAULT",
        )
    }

    override fun renderRenameTable(
        oldTableName: String,
        newTableName: String,
    ): List<String> {
        val oldTable = quoteIdentifier(oldTableName)
        val newTable = quoteIdentifier(newTableName)
        return listOf(
            """
            DO ${'$'}${'$'}
            BEGIN
              IF to_regclass('${oldTableName.escapeSqlLiteral()}') IS NOT NULL
                 AND to_regclass('${newTableName.escapeSqlLiteral()}') IS NULL THEN
                ALTER TABLE $oldTable RENAME TO $newTable;
              END IF;
            END ${'$'}${'$'}
            """.trimIndent()
        )
    }

    override fun renderAddPrimaryKey(tableName: String, columnNames: List<String>): List<String> {
        val table = quoteIdentifier(tableName)
        val columns = columnNames.joinToString(", ") { columnName -> quoteIdentifier(columnName) }
        return listOf(
            """
            DO ${'$'}${'$'}
            BEGIN
              IF to_regclass('${tableName.escapeSqlLiteral()}') IS NOT NULL
                 AND NOT EXISTS (
                   SELECT 1
                   FROM pg_constraint
                   WHERE conrelid = to_regclass('${tableName.escapeSqlLiteral()}')
                     AND contype = 'p'
                 ) THEN
                ALTER TABLE $table ADD PRIMARY KEY ($columns);
              END IF;
            END ${'$'}${'$'}
            """.trimIndent()
        )
    }

    override fun renderDropPrimaryKey(tableName: String): List<String> {
        return listOf(
            """
            DO ${'$'}${'$'}
            DECLARE
              primary_key_name TEXT;
            BEGIN
              SELECT conname
              INTO primary_key_name
              FROM pg_constraint
              WHERE conrelid = to_regclass('${tableName.escapeSqlLiteral()}')
                AND contype = 'p';
              IF primary_key_name IS NOT NULL THEN
                EXECUTE format('ALTER TABLE %I DROP CONSTRAINT %I', '${tableName.escapeSqlLiteral()}', primary_key_name);
              END IF;
            END ${'$'}${'$'}
            """.trimIndent()
        )
    }

    override fun renderAlterColumn(
        tableName: String,
        column: AutoDdlColumn,
        previousColumn: AutoDdlColumn?,
    ): List<String> {
        val table = quoteIdentifier(tableName)
        val columnName = quoteIdentifier(column.name)
        return buildList {
            val shouldAlterType = previousColumn == null || column.hasDifferentTypeFrom(previousColumn)
            if (shouldAlterType) {
                add("ALTER TABLE $table ALTER COLUMN $columnName DROP DEFAULT")
                add(
                    "ALTER TABLE $table\n" +
                        "  ALTER COLUMN $columnName TYPE ${columnType(column)}\n" +
                        "  USING ${column.castExpression(columnName, previousColumn)}"
                )
            }

            if (previousColumn == null || column.nullable != previousColumn.nullable) {
                add(
                    if (column.nullable) {
                        "ALTER TABLE $table ALTER COLUMN $columnName DROP NOT NULL"
                    } else {
                        "ALTER TABLE $table ALTER COLUMN $columnName SET NOT NULL"
                    }
                )
            }

            if (column.defaultValue != null) {
                if (previousColumn == null || column.defaultValue.normalizedDefault() != previousColumn.defaultValue.normalizedDefault()) {
                    add("ALTER TABLE $table ALTER COLUMN $columnName SET DEFAULT ${column.defaultValue}")
                }
            } else if (!shouldAlterType && previousColumn.defaultValue != null) {
                add("ALTER TABLE $table ALTER COLUMN $columnName DROP DEFAULT")
            }
        }
    }

    override fun renderDropColumnNotNull(tableName: String, columnName: String): List<String> {
        val table = quoteIdentifier(tableName)
        val column = quoteIdentifier(columnName)
        return listOf("ALTER TABLE $table ALTER COLUMN $column DROP NOT NULL")
    }

    override fun renderDropIndex(tableName: String, indexName: String): String {
        return "DROP INDEX IF EXISTS ${quoteIdentifier(indexName)}"
    }

    override fun renderSetColumnNotNull(tableName: String, column: AutoDdlColumn): List<String> {
        val table = quoteIdentifier(tableName)
        val columnName = quoteIdentifier(column.name)
        val fillValue = column.defaultValue?.takeIf { it.isNotBlank() } ?: column.fallbackDefaultValue()
        return listOf(
            "UPDATE $table SET $columnName = $fillValue WHERE $columnName IS NULL",
            "ALTER TABLE $table ALTER COLUMN $columnName SET NOT NULL",
        )
    }

    override fun renderCreateIndex(tableName: String, index: AutoDdlIndex): String {
        val indexKeyword = when (index.type) {
            AutoDdlIndexType.UNIQUE -> "UNIQUE INDEX"
            AutoDdlIndexType.FULLTEXT -> "INDEX"
            AutoDdlIndexType.NORMAL -> "INDEX"
        }
        val columns = index.columnNames.joinToString(", ") { quoteIdentifier(it) }
        val predicate = if (index.type == AutoDdlIndexType.UNIQUE && index.ignoreBlankValues) {
            index.columnNames.joinToString(" AND ", prefix = " WHERE ") { columnName ->
                "${quoteIdentifier(columnName)} IS NOT NULL AND ${quoteIdentifier(columnName)} <> ''"
            }
        } else {
            ""
        }
        return "CREATE $indexKeyword IF NOT EXISTS ${quoteIdentifier(index.name)} ON ${quoteIdentifier(tableName)} ($columns)$predicate"
    }

    override fun renderAddForeignKey(
        tableName: String,
        foreignKey: AutoDdlForeignKey,
    ): List<String> {
        val columns = foreignKey.columnNames.joinToString(", ") { quoteIdentifier(it) }
        val referencedColumns = foreignKey.referencedColumnNames.joinToString(", ") { quoteIdentifier(it) }
        val constraint = quoteIdentifier(foreignKey.name)
        val table = quoteIdentifier(tableName)
        val referencedTable = quoteIdentifier(foreignKey.referencedTableName)
        val onDelete = foreignKey.onDelete?.takeIf { it.isNotBlank() }?.let { " ON DELETE $it" }.orEmpty()
        val onUpdate = foreignKey.onUpdate?.takeIf { it.isNotBlank() }?.let { " ON UPDATE $it" }.orEmpty()
        return listOf(
            """
            DO ${'$'}${'$'}
            BEGIN
              IF to_regclass('${tableName.escapeSqlLiteral()}') IS NOT NULL
                 AND to_regclass('${foreignKey.referencedTableName.escapeSqlLiteral()}') IS NOT NULL
                 AND NOT EXISTS (
                   SELECT 1
                   FROM pg_constraint
                   WHERE conname = '${foreignKey.name.escapeSqlLiteral()}'
                     AND conrelid = to_regclass('${tableName.escapeSqlLiteral()}')
                 ) THEN
                ALTER TABLE $table ADD CONSTRAINT $constraint FOREIGN KEY ($columns) REFERENCES $referencedTable ($referencedColumns)$onDelete$onUpdate;
              END IF;
            END ${'$'}${'$'};
            """.trimIndent()
        )
    }

    override fun renderAutoIncrementClause(column: AutoDdlColumn): String? {
        return if (column.autoIncrement) "GENERATED BY DEFAULT AS IDENTITY" else null
    }

    override fun columnType(column: AutoDdlColumn): String {
        return when (column.logicalType) {
            AutoDdlLogicalType.STRING -> column.length?.let { length -> "VARCHAR($length)" } ?: "TEXT"
            AutoDdlLogicalType.TEXT -> "TEXT"
            AutoDdlLogicalType.CHAR -> "CHAR(${column.length ?: 1})"
            AutoDdlLogicalType.BOOLEAN -> "BOOLEAN"
            AutoDdlLogicalType.INT8 -> "SMALLINT"
            AutoDdlLogicalType.INT16 -> "SMALLINT"
            AutoDdlLogicalType.INT32 -> "INTEGER"
            AutoDdlLogicalType.INT64 -> "BIGINT"
            AutoDdlLogicalType.DECIMAL -> "NUMERIC(${column.precision ?: 19}, ${column.scale ?: 2})"
            AutoDdlLogicalType.BIG_INTEGER -> "NUMERIC(65, 0)"
            AutoDdlLogicalType.FLOAT32 -> "REAL"
            AutoDdlLogicalType.FLOAT64 -> "DOUBLE PRECISION"
            AutoDdlLogicalType.DATE -> "DATE"
            AutoDdlLogicalType.TIME -> "TIME"
            AutoDdlLogicalType.DATETIME, AutoDdlLogicalType.TIMESTAMP -> "TIMESTAMP"
            AutoDdlLogicalType.DATETIME_TZ -> "TIMESTAMP WITH TIME ZONE"
            AutoDdlLogicalType.DURATION -> "INTERVAL"
            AutoDdlLogicalType.BINARY -> "BYTEA"
            AutoDdlLogicalType.UUID -> "UUID"
            AutoDdlLogicalType.JSON -> column.nativeTypeHint?.takeIf { it.isNotBlank() } ?: "JSONB"
            AutoDdlLogicalType.UNKNOWN -> column.nativeTypeHint ?: "TEXT"
        }
    }

    private fun String.escapeSqlLiteral(): String {
        return replace("'", "''")
    }

    private fun AutoDdlColumn.hasDifferentTypeFrom(previousColumn: AutoDdlColumn): Boolean {
        return columnType(this) != columnType(previousColumn)
    }

    private fun AutoDdlColumn.castExpression(
        columnName: String,
        previousColumn: AutoDdlColumn?,
    ): String {
        val previousType = previousColumn?.logicalType
        if (previousType == null || previousType !in STRING_LIKE_TYPES || logicalType !in NUMERIC_TYPES) {
            return "$columnName::${columnType(this)}"
        }

        val textValue = "btrim($columnName::text)"
        val fallbackValue = if (nullable) {
            "NULL"
        } else {
            defaultValue?.takeIf { it.isNotBlank() } ?: fallbackDefaultValue()
        }
        val numericPattern = if (logicalType in INTEGER_TYPES) INTEGER_PATTERN else DECIMAL_PATTERN
        return """
            CASE
              WHEN $columnName IS NULL OR $textValue = '' THEN $fallbackValue
              WHEN $textValue ~ '$numericPattern' THEN $textValue::${columnType(this)}
              ELSE $fallbackValue
            END
        """.trimIndent()
    }

    private fun String?.normalizedDefault(): String? {
        return this?.trim()?.removeSurrounding("'")?.lowercase()
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
            AutoDdlLogicalType.BINARY -> "decode('', 'hex')"
            AutoDdlLogicalType.JSON -> "'{}'::jsonb"
        }
    }

    private companion object {
        val STRING_LIKE_TYPES = setOf(
            AutoDdlLogicalType.STRING,
            AutoDdlLogicalType.TEXT,
            AutoDdlLogicalType.CHAR,
            AutoDdlLogicalType.UNKNOWN,
        )
        val INTEGER_TYPES = setOf(
            AutoDdlLogicalType.INT8,
            AutoDdlLogicalType.INT16,
            AutoDdlLogicalType.INT32,
            AutoDdlLogicalType.INT64,
            AutoDdlLogicalType.BIG_INTEGER,
        )
        val NUMERIC_TYPES = INTEGER_TYPES + setOf(
            AutoDdlLogicalType.DECIMAL,
            AutoDdlLogicalType.FLOAT32,
            AutoDdlLogicalType.FLOAT64,
        )
        const val INTEGER_PATTERN = "^[+-]?[0-9]+$"
        const val DECIMAL_PATTERN = "^[+-]?([0-9]+(\\.[0-9]+)?|\\.[0-9]+)$"
    }
}

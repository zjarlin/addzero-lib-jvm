package site.addzero.util.ddlgenerator.strategy

import org.koin.core.annotation.Single
import site.addzero.util.db.DatabaseType
import site.addzero.util.str.makeSurroundWith
import site.addzero.util.lsi.database.dialect.DdlGenerationStrategy
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.clazz.guessTableName
import site.addzero.util.lsi.database.model.ForeignKeyInfo
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi_impl.impl.database.clazz.getAllDbFields
import site.addzero.util.lsi_impl.impl.database.clazz.getDatabaseForeignKeys
import site.addzero.util.lsi_impl.impl.database.field.*

/**
 * SQLite方言的DDL生成策略
 * SQLite has limited type system (only 5 storage classes: NULL, INTEGER, REAL, TEXT, BLOB)
 */
@Single
class SQLiteDdlStrategy : DdlGenerationStrategy {

    override val quoteIdentifier: String = "\""

    override fun support(databaseType: DatabaseType): Boolean =
        databaseType == DatabaseType.SQLITE

    override fun getColumnTypeString(lsiField: LsiField): String =
        lsiField.typeName?.let { typeName ->
            when (typeName) {
                // Integer types - SQLite uses INTEGER for all
                "Integer", "Long", "Short", "Byte" -> "INTEGER"

                // Floating point types
                "Float", "Double" -> "REAL"
                "BigDecimal", "BigInteger" -> "TEXT"

                // String types
                "String" -> "TEXT"

                // Character types
                "Character", "Char" -> "TEXT"

                // Boolean type
                "Boolean" -> "INTEGER"

                // Date/Time types - SQLite stores as TEXT or INTEGER
                "Date", "sqlDate", "sqlTime", "sqlTimestamp",
                "LocalDate", "LocalTime", "LocalDateTime",
                "ZonedDateTime", "OffsetDateTime", "Instant" -> "TEXT"
                "Duration" -> "INTEGER"

                // Binary types
                "byte[]", "[B" -> "BLOB"

                // UUID type
                "UUID" -> "TEXT"

                // JSON type
                "JsonNode" -> "TEXT"

                // Array types - SQLite stores as TEXT (JSON)
                "Integer[]", "[Ljava.lang.Integer;", "IntArray",
                "Long[]", "[Ljava.lang.Long;", "LongArray",
                "String[]", "[Ljava.lang.String;", "Array<String>" -> "TEXT"

                else -> "TEXT"
            }
        } ?: "TEXT"

    override fun generateCreateTable(klass: LsiClass): String {
        val tableName = klass.guessTableName
        val columns = klass.getAllDbFields()

        val columnsSql = columns.joinToString(",\n  ") { buildColumnDefinition(it) }

        return """
            |CREATE TABLE ${tableName.makeSurroundWith(quoteIdentifier)} (
            |  $columnsSql
            |);
            """.trimMargin()
    }

    override fun generateDropTable(tableName: String): String =
        "DROP TABLE IF EXISTS ${tableName.makeSurroundWith(quoteIdentifier)};"

    override fun generateAddColumn(tableName: String, field: LsiField): String =
        "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ADD COLUMN ${buildColumnDefinition(field)};"

    override fun generateDropColumn(tableName: String, columnName: String): String =
        "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} DROP COLUMN ${columnName.makeSurroundWith(quoteIdentifier)};"

    override fun generateModifyColumn(tableName: String, field: LsiField): String =
        throw UnsupportedOperationException("SQLite does not support ALTER COLUMN. Use table recreation instead.")

    override fun generateAddForeignKey(tableName: String, foreignKey: ForeignKeyInfo): String =
        throw UnsupportedOperationException("SQLite foreign keys must be defined during table creation. Use table recreation instead.")

    override fun generateAddComment(lsiClass: LsiClass): String =
        "" // SQLite does not support comments

    override fun generateAll(lsiClasses: List<LsiClass>): String {
        val statements = mutableListOf<String>()

        // Enable foreign key constraints
        statements.add("PRAGMA foreign_keys = ON")

        // Create tables (with inline foreign keys)
        statements.addAll(lsiClasses.map { generateCreateTableWithForeignKeys(it) })

        return statements.joinToString(";\n\n") + ";"
    }

    private fun generateCreateTableWithForeignKeys(klass: LsiClass): String {
        val tableName = klass.guessTableName
        val columns = klass.getAllDbFields()
        val foreignKeys = klass.getDatabaseForeignKeys()

        val columnsSql = columns.joinToString(",\n  ") { buildColumnDefinition(it) }

        val foreignKeysSql = foreignKeys.takeIf { it.isNotEmpty() }
            ?.joinToString(",\n  ", prefix = ",\n  ") { fk ->
                "FOREIGN KEY (${fk.columnName.makeSurroundWith(quoteIdentifier)}) REFERENCES ${fk.referencedTableName.makeSurroundWith(quoteIdentifier)} (${fk.referencedColumnName.makeSurroundWith(quoteIdentifier)})"
            } ?: ""

        return """
            |CREATE TABLE ${tableName.makeSurroundWith(quoteIdentifier)} (
            |  $columnsSql$foreignKeysSql
            |)
            """.trimMargin()
    }

    private fun buildColumnDefinition(field: LsiField): String =
        buildString {
            val columnName = field.columnName ?: field.name ?: "unknown"
            append("${columnName.makeSurroundWith(quoteIdentifier)} ${getColumnTypeString(field)}")

            if (field.isPrimaryKey) {
                append(" PRIMARY KEY")
                if (field.isAutoIncrement) {
                    append(" AUTOINCREMENT")
                }
            }

            if (!field.isNullable) {
                append(" NOT NULL")
            }

            if (field.defaultValue != null && !field.isAutoIncrement) {
                append(" DEFAULT ${field.defaultValue}")
            }
        }
}

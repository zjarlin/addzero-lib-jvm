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
 * H2方言的DDL生成策略
 * H2 supports most MySQL and PostgreSQL syntax
 */
@Single
class H2DdlStrategy : DdlGenerationStrategy {

    override val quoteIdentifier: String = "\""

    override fun support(databaseType: DatabaseType): Boolean =
        databaseType == DatabaseType.H2

    override fun getColumnTypeString(lsiField: LsiField): String =
        lsiField.typeName?.let { typeName ->
            when (typeName) {
                // Integer types
                "Integer" -> "INT"
                "Long" -> "BIGINT"
                "Short" -> "SMALLINT"
                "Byte" -> "TINYINT"

                // Floating point types
                "Float" -> "REAL"
                "Double" -> "DOUBLE"
                "BigDecimal" -> lsiField.run {
                    when {
                        precision > 0 && scale > 0 -> "DECIMAL($precision, $scale)"
                        precision > 0 -> "DECIMAL($precision)"
                        else -> "DECIMAL(19, 2)"
                    }
                }
                "BigInteger" -> "DECIMAL(65, 0)"

                // String types
                "String" -> mapStringType(lsiField)

                // Character types
                "Character", "Char" -> "CHAR(1)"

                // Boolean type
                "Boolean" -> "BOOLEAN"

                // Date/Time types
                "Date", "sqlTimestamp", "LocalDateTime" -> "TIMESTAMP"
                "sqlDate", "LocalDate" -> "DATE"
                "sqlTime", "LocalTime" -> "TIME"
                "ZonedDateTime", "OffsetDateTime", "Instant" -> "TIMESTAMP WITH TIME ZONE"
                "Duration" -> "BIGINT"

                // Binary types
                "byte[]", "[B" -> "BLOB"

                // UUID type (H2 native support)
                "UUID" -> "UUID"

                // JSON type (H2 2.0+)
                "JsonNode" -> "JSON"

                // Array types (H2 supports arrays)
                "Integer[]", "[Ljava.lang.Integer;", "IntArray" -> "ARRAY"
                "Long[]", "[Ljava.lang.Long;", "LongArray" -> "ARRAY"
                "String[]", "[Ljava.lang.String;", "Array<String>" -> "ARRAY"

                else -> "VARCHAR(255)"
            }
        } ?: "VARCHAR(255)"

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
        "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ALTER COLUMN ${buildColumnDefinition(field)};"

    override fun generateAddForeignKey(tableName: String, foreignKey: ForeignKeyInfo): String =
        "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ADD CONSTRAINT ${foreignKey.name.makeSurroundWith(quoteIdentifier)} FOREIGN KEY (${foreignKey.columnName.makeSurroundWith(quoteIdentifier)}) REFERENCES ${foreignKey.referencedTableName.makeSurroundWith(quoteIdentifier)} (${foreignKey.referencedColumnName.makeSurroundWith(quoteIdentifier)});"

    override fun generateAddComment(lsiClass: LsiClass): String =
        buildList {
            val tableName = lsiClass.guessTableName
            val quotedTableName = tableName.makeSurroundWith(quoteIdentifier)

            lsiClass.comment?.let {
                add("COMMENT ON TABLE $quotedTableName IS '$it'")
            }

            lsiClass.getAllDbFields()
                .filter { it.comment != null }
                .forEach { field ->
                    (field.columnName ?: field.name)?.let { columnName ->
                        add("COMMENT ON COLUMN $quotedTableName.${columnName.makeSurroundWith(quoteIdentifier)} IS '${field.comment}'")
                    }
                }
        }.let { if (it.isEmpty()) "" else it.joinToString(";\n") + ";" }

    override fun generateAll(lsiClasses: List<LsiClass>): String {
        val statements = mutableListOf<String>()

        // 1. Create tables
        statements.addAll(lsiClasses.map { generateCreateTable(it) })

        // 2. Add foreign keys
        lsiClasses.forEach { lsiClass ->
            lsiClass.getDatabaseForeignKeys().forEach { fk ->
                statements.add(generateAddForeignKey(lsiClass.guessTableName, fk))
            }
        }

        // 3. Add comments
        lsiClasses
            .filter { it.comment != null || it.getAllDbFields().any { field -> field.comment != null } }
            .forEach { statements.add(generateAddComment(it)) }

        return statements.joinToString("\n\n")
    }

    private fun buildColumnDefinition(field: LsiField): String =
        buildString {
            val columnName = field.columnName ?: field.name ?: "unknown"
            append("${columnName.makeSurroundWith(quoteIdentifier)} ${getColumnTypeString(field)}")

            if (field.isAutoIncrement) {
                append(" AUTO_INCREMENT")
            }

            if (!field.isNullable) {
                append(" NOT NULL")
            }

            if (field.defaultValue != null && !field.isAutoIncrement) {
                append(" DEFAULT ${field.defaultValue}")
            }

            if (field.isPrimaryKey) {
                append(" PRIMARY KEY")
            }
        }

    private fun mapStringType(field: LsiField): String =
        when {
            field.isText -> "CLOB"
            field.length > 0 -> "VARCHAR(${field.length})"
            else -> "VARCHAR(255)"
        }
}

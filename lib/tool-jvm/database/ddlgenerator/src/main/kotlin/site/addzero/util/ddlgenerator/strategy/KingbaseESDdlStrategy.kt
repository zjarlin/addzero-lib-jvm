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
 * KingbaseES (人大金仓) 数据库方言的DDL生成策略
 * KingbaseES is based on PostgreSQL, so the syntax is very similar
 */
@Single
class KingbaseESDdlStrategy : DdlGenerationStrategy {

    override val quoteIdentifier: String = "\""

    override fun support(databaseType: DatabaseType): Boolean =
        databaseType == DatabaseType.KINGBASE

    override fun getColumnTypeString(lsiField: LsiField): String =
        lsiField.typeName?.let { typeName ->
            when (typeName) {
                // Integer types
                "Integer" -> "INTEGER"
                "Long" -> "BIGINT"
                "Short" -> "SMALLINT"
                "Byte" -> "SMALLINT" // KingbaseES doesn't have TINYINT

                // Floating point types
                "Float" -> "REAL"
                "Double" -> "DOUBLE PRECISION"
                "BigDecimal" -> lsiField.run {
                    when {
                        precision > 0 && scale > 0 -> "NUMERIC($precision, $scale)"
                        precision > 0 -> "NUMERIC($precision)"
                        else -> "NUMERIC(19, 2)"
                    }
                }
                "BigInteger" -> "NUMERIC(65, 0)"

                // String types
                "String" -> mapStringType(lsiField)

                // Character types
                "Character", "Char" -> "CHAR(1)"

                // Boolean type
                "Boolean" -> "BOOLEAN"

                // Date/Time types
                "Date" -> "TIMESTAMP"
                "sqlDate" -> "DATE"
                "sqlTime" -> "TIME"
                "sqlTimestamp" -> "TIMESTAMP"
                "LocalDate" -> "DATE"
                "LocalTime" -> "TIME"
                "LocalDateTime" -> "TIMESTAMP"
                "ZonedDateTime", "OffsetDateTime", "Instant" -> "TIMESTAMP WITH TIME ZONE"
                "Duration" -> "INTERVAL"

                // Binary types
                "byte[]", "[B" -> "BYTEA"

                // UUID type
                "UUID" -> "UUID"

                // JSON type
                "JsonNode" -> "JSONB"

                // Array types
                "Integer[]", "[Ljava.lang.Integer;", "IntArray" -> "INTEGER[]"
                "Long[]", "[Ljava.lang.Long;", "LongArray" -> "BIGINT[]"
                "String[]", "[Ljava.lang.String;", "Array<String>" -> "TEXT[]"

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

    override fun generateModifyColumn(tableName: String, field: LsiField): String {
        val columnName = field.columnName ?: field.name ?: "unknown"
        val statements = mutableListOf<String>()

        // KingbaseES follows PostgreSQL pattern: need separate commands
        statements.add("ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ALTER COLUMN ${columnName.makeSurroundWith(quoteIdentifier)} TYPE ${getColumnTypeString(field)};")

        if (!field.isNullable) {
            statements.add("ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ALTER COLUMN ${columnName.makeSurroundWith(quoteIdentifier)} SET NOT NULL;")
        } else {
            statements.add("ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ALTER COLUMN ${columnName.makeSurroundWith(quoteIdentifier)} DROP NOT NULL;")
        }

        if (field.defaultValue != null) {
            statements.add("ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ALTER COLUMN ${columnName.makeSurroundWith(quoteIdentifier)} SET DEFAULT ${field.defaultValue};")
        }

        return statements.joinToString("\n")
    }

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

        // 1. Create sequences for auto-increment fields
        val sequences = mutableSetOf<String>()
        lsiClasses.forEach { lsiClass ->
            lsiClass.getAllDbFields()
                .filter { it.isSequence }
                .forEach { field ->
                    val seqName = field.sequenceName ?: "${field.columnName ?: field.name}_seq"
                    sequences.add(seqName)
                }
        }
        sequences.forEach { seqName ->
            statements.add("CREATE SEQUENCE IF NOT EXISTS ${seqName.makeSurroundWith(quoteIdentifier)} INCREMENT BY 1 START WITH 1")
        }

        // 2. Create tables
        statements.addAll(lsiClasses.map { generateCreateTable(it) })

        // 3. Add constraints and comments
        lsiClasses.forEach { lsiClass ->
            // Foreign keys
            lsiClass.getDatabaseForeignKeys().forEach { fk ->
                statements.add(generateAddForeignKey(lsiClass.guessTableName, fk))
            }

            // Comments
            if (lsiClass.comment != null || lsiClass.getAllDbFields().any { it.comment != null }) {
                statements.add(generateAddComment(lsiClass))
            }
        }

        return statements.joinToString("\n\n")
    }

    private fun buildColumnDefinition(field: LsiField): String =
        buildString {
            val columnName = field.columnName ?: field.name ?: "unknown"
            append("${columnName.makeSurroundWith(quoteIdentifier)} ${getColumnTypeString(field)}")

            if (!field.isNullable) {
                append(" NOT NULL")
            }

            // KingbaseES supports IDENTITY (PostgreSQL 10+ style)
            if (field.isAutoIncrement) {
                append(" GENERATED BY DEFAULT AS IDENTITY")
            } else if (field.isSequence) {
                val seqName = field.sequenceName ?: "${columnName}_seq"
                append(" DEFAULT nextval('$seqName')")
            }

            if (field.defaultValue != null && !field.isAutoIncrement && !field.isSequence) {
                append(" DEFAULT ${field.defaultValue}")
            }

            if (field.isPrimaryKey) {
                append(" PRIMARY KEY")
            }
        }

    private fun mapStringType(field: LsiField): String =
        when {
            field.isText -> "TEXT"
            field.length > 0 -> "VARCHAR(${field.length})"
            else -> "VARCHAR(255)"
        }
}

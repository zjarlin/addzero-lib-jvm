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
 * Oracle方言的DDL生成策略
 */
@Single
class OracleDdlStrategy : DdlGenerationStrategy {

    override val quoteIdentifier: String = "\""

    override fun support(databaseType: DatabaseType): Boolean =
        databaseType == DatabaseType.ORACLE

    override fun getColumnTypeString(lsiField: LsiField): String =
        lsiField.typeName?.let { typeName ->
            when (typeName) {
                // Integer types
                "Integer" -> "NUMBER(10)"
                "Long" -> "NUMBER(19)"
                "Short" -> "NUMBER(5)"
                "Byte" -> "NUMBER(3)"

                // Floating point types
                "Float" -> "BINARY_FLOAT"
                "Double" -> "BINARY_DOUBLE"
                "BigDecimal" -> lsiField.run {
                    when {
                        precision > 0 && scale > 0 -> "NUMBER($precision, $scale)"
                        precision > 0 -> "NUMBER($precision)"
                        else -> "NUMBER(19, 2)"
                    }
                }
                "BigInteger" -> "NUMBER(65)"

                // String types
                "String" -> mapStringType(lsiField)

                // Character types
                "Character", "Char" -> "CHAR(1)"

                // Boolean type
                "Boolean" -> "NUMBER(1)"

                // Date/Time types
                "Date", "sqlDate", "sqlTimestamp", "LocalDate", "LocalDateTime" -> "TIMESTAMP"
                "sqlTime", "LocalTime" -> "TIMESTAMP"
                "ZonedDateTime", "OffsetDateTime" -> "TIMESTAMP WITH TIME ZONE"
                "Instant" -> "TIMESTAMP WITH TIME ZONE"
                "Duration" -> "INTERVAL DAY TO SECOND"

                // Binary types
                "byte[]", "[B" -> "BLOB"

                // UUID type
                "UUID" -> "VARCHAR2(36)"

                // JSON type (Oracle 12c+)
                "JsonNode" -> "CLOB"

                else -> "VARCHAR2(255)"
            }
        } ?: "VARCHAR2(255)"

    override fun generateCreateTable(klass: LsiClass): String {
        val tableName = klass.guessTableName
        val columns = klass.getAllDbFields()

        val columnsSql = columns.joinToString(",\n  ") { buildColumnDefinition(it) }

        return """
            |CREATE TABLE ${tableName.makeSurroundWith(quoteIdentifier)} (
            |  $columnsSql
            |)
            """.trimMargin()
    }

    override fun generateDropTable(tableName: String): String =
        "DROP TABLE ${tableName.makeSurroundWith(quoteIdentifier)} CASCADE CONSTRAINTS"

    override fun generateAddColumn(tableName: String, field: LsiField): String =
        "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ADD (${buildColumnDefinition(field)})"

    override fun generateDropColumn(tableName: String, columnName: String): String =
        "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} DROP COLUMN ${columnName.makeSurroundWith(quoteIdentifier)}"

    override fun generateModifyColumn(tableName: String, field: LsiField): String =
        "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} MODIFY (${buildColumnDefinition(field)})"

    override fun generateAddForeignKey(tableName: String, foreignKey: ForeignKeyInfo): String =
        "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ADD CONSTRAINT ${foreignKey.name.makeSurroundWith(quoteIdentifier)} FOREIGN KEY (${foreignKey.columnName.makeSurroundWith(quoteIdentifier)}) REFERENCES ${foreignKey.referencedTableName.makeSurroundWith(quoteIdentifier)} (${foreignKey.referencedColumnName.makeSurroundWith(quoteIdentifier)})"

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
                        add("COMMENT ON COLUMN ${quotedTableName}.${columnName.makeSurroundWith(quoteIdentifier)} IS '${field.comment}'")
                    }
                }
        }.let { if (it.isEmpty()) "" else it.joinToString(";\n") + ";" }

    override fun generateAll(lsiClasses: List<LsiClass>): String {
        val statements = mutableListOf<String>()

        // 1. Create sequences for auto-increment fields
        lsiClasses.forEach { lsiClass ->
            lsiClass.getAllDbFields()
                .filter { it.isAutoIncrement }
                .forEach { field ->
                    val seqName = "${lsiClass.guessTableName}_${field.columnName ?: field.name}_seq"
                    statements.add("CREATE SEQUENCE ${seqName.makeSurroundWith(quoteIdentifier)} START WITH 1 INCREMENT BY 1")
                }
        }

        // 2. Create tables
        statements.addAll(lsiClasses.map { generateCreateTable(it) })

        // 3. Create triggers for auto-increment
        lsiClasses.forEach { lsiClass ->
            lsiClass.getAllDbFields()
                .filter { it.isAutoIncrement }
                .forEach { field ->
                    val tableName = lsiClass.guessTableName
                    val columnName = field.columnName ?: field.name ?: return@forEach
                    val seqName = "${tableName}_${columnName}_seq"
                    val triggerName = "${tableName}_${columnName}_trg"
                    statements.add("""
                        |CREATE OR REPLACE TRIGGER ${triggerName.makeSurroundWith(quoteIdentifier)}
                        |BEFORE INSERT ON ${tableName.makeSurroundWith(quoteIdentifier)}
                        |FOR EACH ROW
                        |BEGIN
                        |  SELECT ${seqName.makeSurroundWith(quoteIdentifier)}.NEXTVAL INTO :NEW.${columnName.makeSurroundWith(quoteIdentifier)} FROM DUAL;
                        |END;
                    """.trimMargin())
                }
        }

        // 4. Add foreign keys
        lsiClasses.forEach { lsiClass ->
            lsiClass.getDatabaseForeignKeys().forEach { fk ->
                statements.add(generateAddForeignKey(lsiClass.guessTableName, fk))
            }
        }

        // 5. Add comments
        lsiClasses
            .filter { it.comment != null || it.getAllDbFields().any { field -> field.comment != null } }
            .forEach { statements.add(generateAddComment(it)) }

        return statements.joinToString(";\n\n") + ";"
    }

    private fun buildColumnDefinition(field: LsiField): String =
        buildString {
            val columnName = field.columnName ?: field.name ?: "unknown"
            append("${columnName.makeSurroundWith(quoteIdentifier)} ${getColumnTypeString(field)}")

            if (!field.isNullable) append(" NOT NULL")
            if (field.defaultValue != null && !field.isAutoIncrement) append(" DEFAULT ${field.defaultValue}")
            if (field.isPrimaryKey) append(" PRIMARY KEY")
        }

    private fun mapStringType(field: LsiField): String =
        when {
            field.isText -> "CLOB"
            field.length > 0 && field.length <= 4000 -> "VARCHAR2(${field.length})"
            field.length > 4000 -> "CLOB"
            else -> "VARCHAR2(255)"
        }
}

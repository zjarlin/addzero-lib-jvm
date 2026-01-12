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
import site.addzero.util.lsi_impl.impl.database.field.*

/**
 * MySQL方言的DDL生成策略
 */
@Single
class MySqlDdlStrategy : DdlGenerationStrategy {

    override val quoteIdentifier: String = "`"

    override fun support(databaseType: DatabaseType): Boolean =
        databaseType == DatabaseType.MYSQL

    override fun getColumnTypeString(lsiField: LsiField): String =
        lsiField.typeName?.let { typeName ->
            when (typeName) {
                // Integer types
                "Integer" -> "INT"
                "Long" -> "BIGINT"
                "Short" -> "SMALLINT"
                "Byte" -> "TINYINT"

                // Floating point types
                "Float" -> "FLOAT"
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
                "String" -> lsiField.run {
                    when {
                        isText -> "TEXT"
                        length > 0 -> "VARCHAR($length)"
                        else -> "VARCHAR(255)"
                    }
                }

                // Character types
                "Character", "Char" -> "CHAR(1)"

                // Boolean type
                "Boolean" -> "TINYINT(1)"

                // Date/Time types
                "Date" -> "DATETIME"
                "sqlDate" -> "DATE"
                "sqlTime" -> "TIME"
                "sqlTimestamp" -> "TIMESTAMP"
                "LocalDate" -> "DATE"
                "LocalTime" -> "TIME"
                "LocalDateTime" -> "DATETIME"
                "ZonedDateTime", "OffsetDateTime", "Instant" -> "TIMESTAMP"
                "Duration" -> "BIGINT"

                // Binary types
                "byte[]", "[B" -> "BLOB"

                // UUID type
                "UUID" -> "VARCHAR(36)"

                // JSON type
                "JsonNode" -> "JSON"

                else -> "VARCHAR(255)"
            }
        } ?: "VARCHAR(255)"


    override fun generateCreateTable(klass: LsiClass): String {
        val tableName = klass.guessTableName
        val quotedTableName = tableName.makeSurroundWith(quoteIdentifier)
        val columns = klass.getAllDbFields()

        val columnsSql = columns.joinToString(",\n  ") { buildColumnDefinition(it) }
        val autoIncrementOption = columns.find { it.isAutoIncrement }?.let { " AUTO_INCREMENT=1" } ?: ""

        return """
            |CREATE TABLE $quotedTableName (
            |  $columnsSql
            |)$autoIncrementOption;
            """.trimMargin()
    }

    override fun generateDropTable(tableName: String): String =
        "DROP TABLE IF EXISTS ${tableName.makeSurroundWith(quoteIdentifier)};"

    override fun generateAddColumn(tableName: String, field: LsiField): String =
        "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ADD COLUMN ${buildColumnDefinition(field)};"

    override fun generateDropColumn(tableName: String, columnName: String): String =
        "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} DROP COLUMN ${columnName.makeSurroundWith(quoteIdentifier)};"

    override fun generateModifyColumn(tableName: String, field: LsiField): String =
        "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} MODIFY COLUMN ${buildColumnDefinition(field)};"

    override fun generateAddForeignKey(tableName: String, foreignKey: ForeignKeyInfo): String =
        "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ADD CONSTRAINT ${foreignKey.name.makeSurroundWith(quoteIdentifier)} FOREIGN KEY (${foreignKey.columnName.makeSurroundWith(quoteIdentifier)}) REFERENCES ${foreignKey.referencedTableName.makeSurroundWith(quoteIdentifier)} (${foreignKey.referencedColumnName.makeSurroundWith(quoteIdentifier)});"

    override fun generateAddComment(lsiClass: LsiClass): String =
        buildList {
            val tableName = lsiClass.guessTableName
            val quotedTableName = tableName.makeSurroundWith(quoteIdentifier)
            lsiClass.comment?.let { add("ALTER TABLE $quotedTableName COMMENT='$it';") }
            lsiClass.getAllDbFields()
                .filter { it.comment != null }
                .forEach { field ->
                    (field.columnName ?: field.name)?.let { columnName ->
                        add("ALTER TABLE $quotedTableName MODIFY ${columnName.makeSurroundWith(quoteIdentifier)} ${getColumnTypeString(field)} COMMENT '${field.comment}';")
                    }
                }
        }.joinToString("\n")

    private fun buildColumnDefinition(field: LsiField): String =
        buildString {
            val columnName = field.columnName ?: field.name ?: "unknown"
            append("${columnName.makeSurroundWith(quoteIdentifier)} ${getColumnTypeString(field)}")

            if (!field.isNullable) append(" NOT NULL")
            if (field.isAutoIncrement) append(" AUTO_INCREMENT")
            if (field.defaultValue != null && !field.isAutoIncrement) append(" DEFAULT ${field.defaultValue}")
            if (field.isPrimaryKey) append(" PRIMARY KEY")
        }

    private fun mapStringType(field: LsiField): String =
        when {
            field.isText -> "TEXT"
            field.length > 0 -> "VARCHAR(${field.length})"
            else -> "VARCHAR(255)"
        }
}

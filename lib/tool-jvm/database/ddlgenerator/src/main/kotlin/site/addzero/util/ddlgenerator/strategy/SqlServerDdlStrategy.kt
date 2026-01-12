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
 * SQL Server方言的DDL生成策略
 */
@Single
class SqlServerDdlStrategy : DdlGenerationStrategy {

    override val quoteIdentifier: String = "["

    override fun support(databaseType: DatabaseType): Boolean =
        databaseType == DatabaseType.SQLSERVER

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
                "Double" -> "FLOAT"
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
                "Character", "Char" -> "NCHAR(1)"

                // Boolean type
                "Boolean" -> "BIT"

                // Date/Time types
                "Date", "sqlTimestamp", "LocalDateTime" -> "DATETIME2"
                "sqlDate", "LocalDate" -> "DATE"
                "sqlTime", "LocalTime" -> "TIME"
                "ZonedDateTime", "OffsetDateTime" -> "DATETIMEOFFSET"
                "Instant" -> "DATETIME2"
                "Duration" -> "BIGINT"

                // Binary types
                "byte[]", "[B" -> "VARBINARY(MAX)"

                // UUID type
                "UUID" -> "UNIQUEIDENTIFIER"

                // JSON type (SQL Server 2016+)
                "JsonNode" -> "NVARCHAR(MAX)"

                else -> "NVARCHAR(255)"
            }
        } ?: "NVARCHAR(255)"

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
        "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ADD ${buildColumnDefinition(field)};"

    override fun generateDropColumn(tableName: String, columnName: String): String =
        "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} DROP COLUMN ${columnName.makeSurroundWith(quoteIdentifier)};"

    override fun generateModifyColumn(tableName: String, field: LsiField): String =
        "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ALTER COLUMN ${buildColumnDefinition(field)};"

    override fun generateAddForeignKey(tableName: String, foreignKey: ForeignKeyInfo): String =
        "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ADD CONSTRAINT ${foreignKey.name.makeSurroundWith(quoteIdentifier)} FOREIGN KEY (${foreignKey.columnName.makeSurroundWith(quoteIdentifier)}) REFERENCES ${foreignKey.referencedTableName.makeSurroundWith(quoteIdentifier)} (${foreignKey.referencedColumnName.makeSurroundWith(quoteIdentifier)});"

    override fun generateAddComment(lsiClass: LsiClass): String =
        buildList {
            val tableName = lsiClass.guessTableName

            lsiClass.comment?.let {
                add("EXEC sp_addextendedproperty @name = N'MS_Description', @value = N'$it', @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'$tableName'")
            }

            lsiClass.getAllDbFields()
                .filter { it.comment != null }
                .forEach { field ->
                    (field.columnName ?: field.name)?.let { columnName ->
                        add("EXEC sp_addextendedproperty @name = N'MS_Description', @value = N'${field.comment}', @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'$tableName', @level2type = N'COLUMN', @level2name = N'$columnName'")
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
                append(" IDENTITY(1,1)")
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
            field.isText -> "NVARCHAR(MAX)"
            field.length > 0 && field.length <= 4000 -> "NVARCHAR(${field.length})"
            field.length > 4000 -> "NVARCHAR(MAX)"
            else -> "NVARCHAR(255)"
        }
}

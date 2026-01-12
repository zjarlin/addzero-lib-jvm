package site.addzero.util.ddlgenerator.strategy

import org.babyfish.jimmer.config.autoddl.Settings
import org.koin.core.annotation.Single
import site.addzero.util.db.DatabaseType
import site.addzero.util.str.makeSurroundWith
import site.addzero.util.lsi.database.dialect.DdlGenerationStrategy
import site.addzero.util.ddlgenerator.config.databaseType
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.clazz.guessTableName
import site.addzero.util.lsi.database.*
import site.addzero.util.lsi.database.model.ForeignKeyInfo
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi_impl.impl.database.clazz.getAllDbFields
import site.addzero.util.lsi_impl.impl.database.clazz.getDatabaseForeignKeys
import site.addzero.util.lsi_impl.impl.database.field.*

/**
 * PostgreSQL方言的DDL生成策略
 */
@Single
class PostgreSqlDdlStrategy : DdlGenerationStrategy {

    override fun support(databaseType: DatabaseType): Boolean {
        return databaseType== DatabaseType.POSTGRESQL
    }

    override fun getColumnTypeString(lsiField: LsiField): String =
        lsiField.typeName?.let { typeName ->
            when (typeName) {
                // Integer types
                "Integer" -> "INTEGER"
                "Long" -> "BIGINT"
                "Short" -> "SMALLINT"
                "Byte" -> "SMALLINT"

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

        val columnsSql = columns.joinToString(",\n  ") { field ->
            buildColumnDefinition(field)
        }

        return """
            |CREATE TABLE ${tableName.makeSurroundWith(quoteIdentifier)} (
            |  $columnsSql
            |);
            """.trimMargin()
    }

    override fun generateDropTable(tableName: String): String {
        return "DROP TABLE IF EXISTS ${tableName.makeSurroundWith(quoteIdentifier)};"
    }

    override fun generateAddColumn(tableName: String, field: LsiField): String {
        val columnDefinition = buildColumnDefinition(field)
        return "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ADD COLUMN $columnDefinition;"
    }

    override fun generateDropColumn(tableName: String, columnName: String): String {
        return "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} DROP COLUMN ${columnName.makeSurroundWith(quoteIdentifier)};"
    }

    override fun generateModifyColumn(tableName: String, field: LsiField): String {
        val columnName = field.columnName ?: field.name ?: "unknown"
        val statements = mutableListOf<String>()

        // PostgreSQL需要分别修改类型、可空性、默认值
        statements.add("ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ALTER COLUMN ${columnName.makeSurroundWith(quoteIdentifier)} TYPE ${getColumnTypeString(field)};");

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

    override fun generateAddForeignKey(tableName: String, foreignKey: ForeignKeyInfo): String {
        return "ALTER TABLE ${tableName.makeSurroundWith(quoteIdentifier)} ADD CONSTRAINT ${foreignKey.name.makeSurroundWith(quoteIdentifier)} FOREIGN KEY (${foreignKey.columnName.makeSurroundWith(quoteIdentifier)}) REFERENCES ${foreignKey.referencedTableName.makeSurroundWith(quoteIdentifier)} (${foreignKey.referencedColumnName.makeSurroundWith(quoteIdentifier)});"
    }

    override fun generateAddComment(lsiClass: LsiClass): String {
        val statements = mutableListOf<String>()
        val tableName = lsiClass.guessTableName
        val quotedTableName = tableName.makeSurroundWith(quoteIdentifier)

        // 表注释
        if (lsiClass.comment != null) {
            statements.add("COMMENT ON TABLE $quotedTableName IS '${lsiClass.comment}';")
        }

        // 列注释
        lsiClass.getAllDbFields().filter { it.comment != null }.forEach { field ->
            val columnName = field.columnName ?: field.name ?: return@forEach
            statements.add("COMMENT ON COLUMN $quotedTableName.${columnName.makeSurroundWith(quoteIdentifier)} IS '${field.comment}';")
        }

        return statements.joinToString("\n")
    }

    override fun generateAll(lsiClasses: List<LsiClass>): String {
        val statements = mutableListOf<String>()

        // 1. 创建所有需要的序列
        val sequences = mutableSetOf<String>()
        lsiClasses.forEach { lsiClass ->
            lsiClass.getAllDbFields().forEach { field ->
                if (field.isSequence) {
                    val seqName = field.sequenceName ?: "${field.columnName ?: field.name}_seq"
                    sequences.add(seqName)
                }
            }
        }
        sequences.forEach { seqName ->
            statements.add(generateCreateSequence(seqName))
        }

        // 2. 创建表
        val createTableStatements = lsiClasses.map { lsiClass -> generateCreateTable(lsiClass) }
        statements.addAll(createTableStatements)

        // 3. 添加约束和注释
        val addConstraintsStatements = lsiClasses.flatMap { lsiClass ->
            val foreignKeyStatements = lsiClass.getDatabaseForeignKeys().map { fk ->
                generateAddForeignKey(lsiClass.guessTableName, fk)
            }
            val commentStatements = if (lsiClass.comment != null || lsiClass.getAllDbFields().any { it.comment != null }) {
                listOf(generateAddComment(lsiClass))
            } else {
                emptyList()
            }
            foreignKeyStatements + commentStatements
        }
        statements.addAll(addConstraintsStatements)

        return statements.joinToString("\n\n")
    }

    /**
     * 生成创建序列的DDL语句
     */
    private fun generateCreateSequence(sequenceName: String): String {
        return "CREATE SEQUENCE IF NOT EXISTS ${sequenceName.makeSurroundWith(quoteIdentifier)} INCREMENT BY 1 START WITH 1;"
    }

    private fun buildColumnDefinition(field: LsiField): String {
        val builder = StringBuilder()
        val columnName = field.columnName ?: field.name ?: "unknown"

        // 使用新的类型映射方法
        val columnTypeName = getColumnTypeString(field)

        builder.append("${columnName.makeSurroundWith(quoteIdentifier)} $columnTypeName")

        if (!field.isNullable) {
            builder.append(" NOT NULL")
        }

        // PostgreSQL支持三种自增方式：
        // 1. IDENTITY (推荐，PostgreSQL 10+)
        // 2. SERIAL类型 (传统方式)
        // 3. SEQUENCE + DEFAULT nextval() (传统方式)
        if (field.isAutoIncrement) {
            builder.append(" GENERATED BY DEFAULT AS IDENTITY")
        } else if (field.isSequence) {
            // 使用序列：需要配合DEFAULT nextval('seq_name')
            val seqName = field.sequenceName ?: "${columnName}_seq"
            builder.append(" DEFAULT nextval('$seqName')")
        }

        if (field.defaultValue != null && !field.isAutoIncrement && !field.isSequence) {
            builder.append(" DEFAULT ${field.defaultValue}")
        }

        if (field.isPrimaryKey) {
            builder.append(" PRIMARY KEY")
        }

        return builder.toString()
    }

    /**
     * PostgreSQL字符串类型映射
     *
     * PostgreSQL的TEXT类型没有长度限制，性能与VARCHAR相同
     * 推荐策略：
     * - 有明确长度限制的用VARCHAR(n)
     * - 长文本统一用TEXT
     */
    private fun mapStringType(field: LsiField): String {
        // 1. 检查是否为长文本
        if (field.isText) {
            return "TEXT"
        }

        // 2. 普通字符串
        val length = field.length
        return when {
            length > 0 -> "VARCHAR($length)"
            else -> "VARCHAR(255)" // 默认长度
        }
    }

}

package site.addzero.util.ddlgenerator

import site.addzero.util.ddlgenerator.inter.TableContext
import site.addzero.util.ddlgenerator.model.*

/**
 * PostgreSQL方言的DDL生成策略
 */
class PostgreSqlDdlGenerationStrategy : DdlGenerationStrategy {
    private val dependencyResolver = DependencyResolver()
    
    override fun supports(dialect: Dialect): Boolean {
        return dialect == Dialect.POSTGRESQL
    }
    
    override fun generateCreateTable(table: TableDefinition): String {
        val columnsSql = table.columns.joinToString(",\n  ") { column ->
            buildColumnDefinition(column)
        }
        
        return """
            |CREATE TABLE "${table.name}" (
            |  $columnsSql
            |);
            """.trimMargin()
    }

    override fun generateDropTable(tableName: String): String {
        return "DROP TABLE IF EXISTS \"$tableName\";"
    }

    override fun generateAddColumn(tableName: String, column: ColumnDefinition): String {
        val columnDefinition = buildColumnDefinition(column)
        return "ALTER TABLE \"$tableName\" ADD COLUMN $columnDefinition;"
    }

    override fun generateDropColumn(tableName: String, columnName: String): String {
        return "ALTER TABLE \"$tableName\" DROP COLUMN \"$columnName\";"
    }
    
    override fun generateAddForeignKey(tableName: String, foreignKey: ForeignKeyDefinition): String {
        return "ALTER TABLE \"$tableName\" ADD CONSTRAINT \"${foreignKey.name}\" FOREIGN KEY (\"${foreignKey.columnName}\") REFERENCES \"${foreignKey.referencedTable}\" (\"${foreignKey.referencedColumnName}\");"
    }
    
    override fun generateAddComment(table: TableDefinition): String {
        val statements = mutableListOf<String>()
        
        // 表注释
        if (table.comment != null) {
            statements.add("COMMENT ON TABLE \"${table.name}\" IS '${table.comment}';")
        }
        
        // 列注释
        table.columns.filter { it.comment != null }.forEach { column ->
            statements.add("COMMENT ON COLUMN \"${table.name}\".\"${column.name}\" IS '${column.comment}';")
        }
        
        return statements.joinToString("\n")
    }

    override fun generateSchema(tables: List<TableDefinition>): String {
        val createTableStatements = tables.map { table -> generateCreateTable(table) }
        val addConstraintsStatements = tables.flatMap { table ->
            val foreignKeyStatements = table.foreignKeys.map { fk -> 
                generateAddForeignKey(table.name, fk)
            }
            val commentStatements = if (table.comment != null || table.columns.any { it.comment != null }) {
                listOf(generateAddComment(table))
            } else {
                emptyList()
            }
            foreignKeyStatements + commentStatements
        }
        
        return (createTableStatements + addConstraintsStatements).joinToString("\n\n")
    }

    override fun generateSchema(context: TableContext): String {
        // 根据依赖关系解析表的创建顺序
        val orderedTables = dependencyResolver.resolveCreationOrder(context)
        return generateSchema(orderedTables)
    }

    override fun getColumnTypeName(columnType: ColumnType, precision: Int?, scale: Int?): String {
        return when (columnType) {
            ColumnType.INT -> "INTEGER"
            ColumnType.BIGINT -> "BIGINT"
            ColumnType.SMALLINT -> "SMALLINT"
            ColumnType.TINYINT -> "SMALLINT"
            ColumnType.DECIMAL -> {
                if (precision != null && scale != null) {
                    "DECIMAL($precision, $scale)"
                } else if (precision != null) {
                    "DECIMAL($precision)"
                } else {
                    "DECIMAL"
                }
            }
            ColumnType.FLOAT -> "REAL"
            ColumnType.DOUBLE -> "DOUBLE PRECISION"
            ColumnType.VARCHAR -> {
                if (precision != null) {
                    "VARCHAR($precision)"
                } else {
                    "VARCHAR(255)"
                }
            }
            ColumnType.CHAR -> {
                if (precision != null) {
                    "CHAR($precision)"
                } else {
                    "CHAR(255)"
                }
            }
            ColumnType.TEXT -> "TEXT"
            ColumnType.LONGTEXT -> "TEXT"
            ColumnType.DATE -> "DATE"
            ColumnType.TIME -> "TIME"
            ColumnType.DATETIME -> "TIMESTAMP"
            ColumnType.TIMESTAMP -> "TIMESTAMP"
            ColumnType.BOOLEAN -> "BOOLEAN"
            ColumnType.BLOB -> "BYTEA"
            ColumnType.BYTES -> "BYTEA"
        }
    }
    
    private fun buildColumnDefinition(column: ColumnDefinition): String {
        val builder = StringBuilder()
        builder.append("\"${column.name}\" ${getColumnTypeName(column.type)}")
        
        if (!column.nullable) {
            builder.append(" NOT NULL")
        }
        
        if (column.autoIncrement) {
            builder.append(" GENERATED BY DEFAULT AS IDENTITY")
        }
        
        if (column.defaultValue != null) {
            builder.append(" DEFAULT ${column.defaultValue}")
        }
        
        if (column.primaryKey) {
            builder.append(" PRIMARY KEY")
        }
        
        return builder.toString()
    }
}
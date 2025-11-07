package site.addzero.util.ddlgenerator

import site.addzero.util.ddlgenerator.inter.TableContext
import site.addzero.util.ddlgenerator.model.*

/**
 * DDL生成策略接口
 */
interface DdlGenerationStrategy {
    /**
     * 检查此策略是否支持给定的数据库方言
     */
    fun supports(dialect: Dialect): Boolean

    /**
     * 生成创建表的DDL语句（不包含外键约束和注释）
     */
    fun generateCreateTable(table: TableDefinition): String

    /**
     * 生成删除表的DDL语句
     */
    fun generateDropTable(tableName: String): String

    /**
     * 生成添加列的DDL语句
     */
    fun generateAddColumn(tableName: String, column: ColumnDefinition): String

    /**
     * 生成删除列的DDL语句
     */
    fun generateDropColumn(tableName: String, columnName: String): String

    /**
     * 生成添加外键约束的DDL语句
     */
    fun generateAddForeignKey(tableName: String, foreignKey: ForeignKeyDefinition): String
    
    /**
     * 生成添加注释的DDL语句
     */
    fun generateAddComment(table: TableDefinition): String

    /**
     * 获取特定列类型的数据库表示形式
     */
    fun getColumnTypeName(columnType: ColumnType, precision: Int? = null, scale: Int? = null): String
    
    /**
     * 生成基于多个表定义的完整DDL语句（考虑表之间的依赖关系）
     * 例如外键约束等需要在所有相关表创建之后才能添加的语句
     */
    fun generateSchema(tables: List<TableDefinition>): String {
        // 默认实现：先创建所有表，然后添加外键约束和注释
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
    
    /**
     * 基于表上下文生成完整的数据库模式
     * 考虑表之间的依赖关系和约束
     */
    fun generateSchema(context: TableContext): String {
        val tables = context.getTableDefinitions()
        return generateSchema(tables)
    }
}
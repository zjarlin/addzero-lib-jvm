package site.addzero.util.ddlgenerator.core

import site.addzero.util.db.DatabaseType
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi.database.DatabaseColumnType
import site.addzero.util.lsi.database.ForeignKeyInfo

/**
 * DDL 方言接口
 * 
 * 定义了生成特定数据库 DDL 语句的契约
 * 使用策略模式，每个数据库方言实现此接口
 */
interface DdlDialect {
    
    /**
     * 支持的数据库类型
     */
    val databaseType: DatabaseType
    
    /**
     * 方言名称（用于日志和调试）
     */
    val dialectName: String
        get() = databaseType.desc
    
    /**
     * 检查此方言是否支持给定的数据库类型
     */
    fun supports(type: DatabaseType): Boolean = this.databaseType == type
    
    /**
     * 生成创建表的 DDL 语句（不包含外键约束和注释）
     */
    fun generateCreateTable(lsiClass: LsiClass): String
    
    /**
     * 生成删除表的 DDL 语句
     */
    fun generateDropTable(tableName: String): String
    
    /**
     * 生成添加列的 DDL 语句
     */
    fun generateAddColumn(tableName: String, field: LsiField): String
    
    /**
     * 生成删除列的 DDL 语句
     */
    fun generateDropColumn(tableName: String, columnName: String): String
    
    /**
     * 生成添加外键约束的 DDL 语句
     */
    fun generateAddForeignKey(tableName: String, foreignKey: ForeignKeyInfo): String
    
    /**
     * 生成添加注释的 DDL 语句
     */
    fun generateAddComment(lsiClass: LsiClass): String
    
    /**
     * 获取特定列类型的数据库表示形式
     */
    fun getColumnTypeName(
        columnType: DatabaseColumnType,
        precision: Int? = null,
        scale: Int? = null
    ): String
    
    /**
     * 生成基于多个 LSI 类的完整 DDL 语句
     */
    fun generateSchema(lsiClasses: List<LsiClass>): String {
        val createTableStatements = lsiClasses.map { generateCreateTable(it) }
        return createTableStatements.joinToString("\n\n")
    }
}

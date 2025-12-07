package site.addzero.util.ddlgenerator.core

import site.addzero.util.db.DatabaseType
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi.database.ForeignKeyInfo

/**
 * DDL 生成器
 * 
 * 门面模式，统一入口
 * 内部使用方言策略生成特定数据库的 DDL
 */
class DdlGenerator(
    private val dialect: DdlDialect
) {
    
    constructor(databaseType: DatabaseType, dialectRegistry: DdlDialectRegistry) : this(
        dialectRegistry.getDialect(databaseType)
    )
    
    /**
     * 生成创建表的 DDL
     */
    fun generateCreateTable(lsiClass: LsiClass): String {
        return dialect.generateCreateTable(lsiClass)
    }
    
    /**
     * 生成删除表的 DDL
     */
    fun generateDropTable(tableName: String): String {
        return dialect.generateDropTable(tableName)
    }
    
    /**
     * 生成添加列的 DDL
     */
    fun generateAddColumn(tableName: String, field: LsiField): String {
        return dialect.generateAddColumn(tableName, field)
    }
    
    /**
     * 生成删除列的 DDL
     */
    fun generateDropColumn(tableName: String, columnName: String): String {
        return dialect.generateDropColumn(tableName, columnName)
    }
    
    /**
     * 生成添加外键的 DDL
     */
    fun generateAddForeignKey(tableName: String, foreignKey: ForeignKeyInfo): String {
        return dialect.generateAddForeignKey(tableName, foreignKey)
    }
    
    /**
     * 生成添加注释的 DDL
     */
    fun generateAddComment(lsiClass: LsiClass): String {
        return dialect.generateAddComment(lsiClass)
    }
    
    /**
     * 生成完整的数据库模式 DDL
     */
    fun generateSchema(lsiClasses: List<LsiClass>): String {
        return dialect.generateSchema(lsiClasses)
    }
    
    companion object {
        /**
         * 创建指定数据库类型的生成器
         */
        fun forDatabase(
            databaseType: DatabaseType,
            dialectRegistry: DdlDialectRegistry = DdlDialectRegistry.getInstance()
        ): DdlGenerator {
            return DdlGenerator(databaseType, dialectRegistry)
        }
    }
}

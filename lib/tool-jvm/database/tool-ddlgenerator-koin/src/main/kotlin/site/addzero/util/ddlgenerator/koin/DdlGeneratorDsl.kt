package site.addzero.util.ddlgenerator.koin

import site.addzero.util.db.DatabaseType
import site.addzero.util.ddlgenerator.core.DdlGenerator
import site.addzero.util.lsi.clazz.LsiClass

/**
 * DDL Generator DSL 扩展函数
 * 
 * 提供便捷的 Kotlin DSL 使用方式
 */

/**
 * 为指定数据库类型创建 DDL 生成器
 */
fun ddlGenerator(databaseType: DatabaseType): DdlGenerator {
    // 确保已初始化
    if (!DdlGeneratorKoinInitializer.isInitialized()) {
        DdlGeneratorKoinInitializer.initialize()
    }
    
    return DdlGenerator.forDatabase(databaseType)
}

/**
 * 使用 DSL 方式生成 DDL
 */
inline fun generateDdl(
    databaseType: DatabaseType,
    block: DdlGenerator.() -> String
): String {
    val generator = ddlGenerator(databaseType)
    return generator.block()
}

/**
 * 为 LsiClass 扩展生成 DDL 的便捷方法
 */
fun LsiClass.toCreateTableSql(databaseType: DatabaseType): String {
    return ddlGenerator(databaseType).generateCreateTable(this)
}

/**
 * 为 LsiClass 列表扩展生成完整 schema 的便捷方法
 */
fun List<LsiClass>.toSchemaSql(databaseType: DatabaseType): String {
    return ddlGenerator(databaseType).generateSchema(this)
}

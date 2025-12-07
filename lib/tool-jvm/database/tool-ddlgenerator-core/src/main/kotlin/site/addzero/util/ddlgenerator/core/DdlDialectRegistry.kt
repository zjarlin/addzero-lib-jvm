package site.addzero.util.ddlgenerator.core

import site.addzero.util.db.DatabaseType
import java.util.concurrent.ConcurrentHashMap

/**
 * DDL 方言注册表
 * 
 * 单例模式，管理所有已注册的方言
 * 支持运行时动态注册新方言
 */
class DdlDialectRegistry private constructor() {
    
    private val dialects = ConcurrentHashMap<DatabaseType, DdlDialect>()
    
    /**
     * 注册方言
     */
    fun register(dialect: DdlDialect) {
        dialects[dialect.databaseType] = dialect
    }
    
    /**
     * 获取指定数据库类型的方言
     */
    fun getDialect(databaseType: DatabaseType): DdlDialect {
        return dialects[databaseType]
            ?: throw IllegalArgumentException("Unsupported database type: ${databaseType.desc}")
    }
    
    /**
     * 检查是否支持指定数据库类型
     */
    fun supports(databaseType: DatabaseType): Boolean {
        return dialects.containsKey(databaseType)
    }
    
    /**
     * 获取所有已注册的数据库类型
     */
    fun getSupportedDatabases(): Set<DatabaseType> {
        return dialects.keys.toSet()
    }
    
    companion object {
        @Volatile
        private var instance: DdlDialectRegistry? = null
        
        fun getInstance(): DdlDialectRegistry {
            return instance ?: synchronized(this) {
                instance ?: DdlDialectRegistry().also { instance = it }
            }
        }
    }
}

package site.addzero.util.ddlgenerator.koin

import org.koin.core.context.GlobalContext
import org.koin.ksp.generated.module
import site.addzero.util.ddlgenerator.core.DdlDialect
import site.addzero.util.ddlgenerator.core.DdlDialectRegistry

/**
 * DDL Generator Koin 初始化器
 * 
 * 负责：
 * 1. 加载 Koin 模块
 * 2. 从 Koin 容器中获取所有方言实现
 * 3. 注册到 DdlDialectRegistry
 */
object DdlGeneratorKoinInitializer {
    
    @Volatile
    private var initialized = false
    
    /**
     * 初始化 DDL Generator Koin 集成
     * 
     * 此方法应该在应用启动时调用
     * 它会自动扫描并注册所有带 @Single 注解的方言实现
     */
    fun initialize() {
        if (initialized) return
        
        synchronized(this) {
            if (initialized) return
            
            // 加载 Koin 模块
            val koin = GlobalContext.getOrNull() ?: run {
                throw IllegalStateException(
                    "Koin is not initialized. Please call startKoin() first."
                )
            }
            
            // 加载 DDL Generator 模块
            koin.loadModules(listOf(DdlGeneratorModule().module))
            
            // 从 Koin 获取所有方言实现并注册
            val dialects = koin.getAll<DdlDialect>()
            val registry = DdlDialectRegistry.getInstance()
            
            dialects.forEach { dialect ->
                registry.register(dialect)
                println("Registered DDL dialect: ${dialect.dialectName} (${dialect.databaseType})")
            }
            
            initialized = true
        }
    }
    
    /**
     * 检查是否已初始化
     */
    fun isInitialized(): Boolean = initialized
}

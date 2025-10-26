package site.addzero.ide.config.scanner

import site.addzero.ide.config.annotation.Route
import site.addzero.ide.config.registry.ConfigRegistry
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * 配置扫描器
 * 负责扫描和注册带 @Route 注解的配置类
 */
object ConfigScanner {
    private var isScanned = false
    
    /**
     * 扫描并注册所有带 @Route 注解的配置类
     */
    fun scanAndRegisterConfigs() {
        if (isScanned) return
        
        // 注册示例配置类
        try {
            // 注册数据库配置
            val databaseConfigClass = Class.forName("site.addzero.ide.config.example.DatabaseConfig").kotlin
            registerIfRouted(databaseConfigClass)
        } catch (e: ClassNotFoundException) {
            // 忽略，示例类可能不存在
        }
        
        try {
            // 注册常用配置
            val usefulConfigClass = Class.forName("site.addzero.ide.config.example.UsefulConfig").kotlin
            registerIfRouted(usefulConfigClass)
        } catch (e: ClassNotFoundException) {
            // 忽略，示例类可能不存在
        }
        
        try {
            // 注册性能配置
            val performanceConfigClass = Class.forName("site.addzero.ide.config.example.PerformanceConfig").kotlin
            registerIfRouted(performanceConfigClass)
        } catch (e: ClassNotFoundException) {
            // 忽略，示例类可能不存在
        }
        
        isScanned = true
    }
    
    /**
     * 如果类带有 @Route 注解，则注册它
     */
    private fun registerIfRouted(configClass: KClass<*>) {
        if (configClass.findAnnotation<Route>() != null) {
            ConfigRegistry.registerConfig(configClass)
        }
    }
    
    /**
     * 注册指定的配置类
     */
    fun registerConfig(configClass: KClass<*>) {
        ConfigRegistry.registerConfig(configClass)
    }
}
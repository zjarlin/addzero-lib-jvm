package com.addzero.gradle.plugin.automodules

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/**
 * AutoModules Gradle 插件主类
 * 
 * 这是一个 Settings 插件，用于在项目配置阶段自动扫描和包含模块
 */
class AutoModulesPlugin : Plugin<Settings> {
    
    companion object {
        const val EXTENSION_NAME = "autoModules"
        
        // 存储scanner实例，以便其他地方可以访问findProject功能
        @Volatile
        private var scannerInstance: ModuleScanner? = null
        
        /**
         * 获取scanner实例（用于findProject功能）
         * 注意：只有在settings评估完成后才可用
         */
        @JvmStatic
        fun getScanner(): ModuleScanner? = scannerInstance
    }
    
    override fun apply(settings: Settings) {
        // 创建扩展配置
        val extension = settings.extensions.create(EXTENSION_NAME, AutoModulesExtension::class.java)
        
        // 在settings评估完成后执行扫描
        settings.gradle.settingsEvaluated {
            val scanner = ModuleScanner(settings, extension)
            scannerInstance = scanner // 保存实例
            scanner.scanAndIncludeModules()
        }
    }
}

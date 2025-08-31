package com.addzero.gradle.plugin.automodules

/**
 * 自动模块插件的扩展配置
 */
open class AutoModulesExtension {

    /**
     * 是否启用全自动扫描模式
     * 当为true时，会自动扫描项目根目录下所有子目录，找到包含build.gradle.kts的叶子目录
     * 当为false时，使用rootModules指定的目录进行扫描
     */
    var autoScan: Boolean = true

    /**
     * 根模块列表，这些模块会被扫描子模块
     * 只在autoScan=false时生效
     */
    var rootModules: List<String> = listOf(".")

    /**
     * 排除的模块列表（支持路径匹配和通配符）
     */
    var excludeModules: List<String> = emptyList()

    /**
     * 是否启用详细日志输出
     */
    var verbose: Boolean = true

    /**
     * 自定义排除的目录名称
     */
    var customExcludedDirs: Set<String> = emptySet()

    /**
     * 是否自动排除测试相关模块
     */
    var excludeTestModules: Boolean = false

    /**
     * DSL 方法：设置根模块
     */
    fun rootModules(vararg modules: String) {
        rootModules = modules.toList()
        autoScan = false  // 手动指定根模块时禁用自动扫描
    }

    /**
     * DSL 方法：启用或禁用全自动扫描
     */
    fun autoScan(enabled: Boolean) {
        autoScan = enabled
    }

    /**
     * DSL 方法：设置排除模块
     */
    fun excludeModules(vararg modules: String) {
        excludeModules = modules.toList()
    }

    /**
     * DSL 方法：添加自定义排除目录
     */
    fun customExcludedDirs(vararg dirs: String) {
        customExcludedDirs = dirs.toSet()
    }

}

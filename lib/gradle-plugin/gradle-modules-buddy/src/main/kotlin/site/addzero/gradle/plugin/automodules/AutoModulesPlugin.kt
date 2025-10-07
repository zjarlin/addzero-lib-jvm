package site.addzero.gradle.plugin.automodules

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import site.addzero.gradle.tool.autoIncludeModules
import java.io.File

/**
 * 自动模块插件扩展配置
 */
open class AutoModulesPluginExtension {
    /**
     * 默认的黑名单目录列表
     */
    val excludeModules = emptyArray<String>()

    val preidicate: ((File) -> Boolean)? = null
}

/**
 * 自动模块插件
 * 自动扫描项目目录并包含所有Gradle子项目
 */
class AutoModulesPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val extension = settings.extensions.create("autoModules", AutoModulesPluginExtension::class.java)
        val excludeModules = extension.excludeModules

        val preidicate = extension.preidicate
        if (preidicate != null) {
            settings.autoIncludeModules(preidicate)
        } else {
            settings.autoIncludeModules(*excludeModules)
        }
    }
}


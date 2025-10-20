package site.addzero.gradle.plugin.automodules

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.Property
import site.addzero.gradle.tool.autoIncludeModules
import java.io.File

/**
 * 自动模块插件扩展配置
 */
interface AutoModulesPluginExtension {
    /**
     * 默认的黑名单目录列表
     */
    val excludeModules: Property<Array<String>>

    /**
     * 函数式判断逻辑 - 返回 Boolean
     */
    val predicate: Property<((File) -> Boolean)?>
}

/**
 * 自动模块插件
 * 自动扫描项目目录并包含所有Gradle子项目
 */
class AutoModulesPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val extension = settings.extensions.create("autoModules", AutoModulesPluginExtension::class.java).apply {
            excludeModules.convention(arrayOf())
            predicate.convention(null)
        }
        val excludeModules = extension.excludeModules

        val preidicate = extension.predicate.get()
        if (preidicate != null) {
            settings.autoIncludeModules(preidicate)
        } else {
            settings.autoIncludeModules(*excludeModules.get())
        }
    }
}


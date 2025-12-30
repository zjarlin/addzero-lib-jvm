package site.addzero.gradle.plugin

import org.gradle.api.provider.Property
import site.addzero.gradle.tool.autoIncludeModules

/**
 * 自动模块插件扩展配置
 */
interface AutoModulesPluginExtension {
    val excludeModules: Property<Array<String>>
}

val extension = extensions.create("autoModules", AutoModulesPluginExtension::class.java).apply {
    val arrayOf = arrayOf<String>()
    excludeModules.convention(arrayOf)
}

autoIncludeModules(*extension.excludeModules.get())

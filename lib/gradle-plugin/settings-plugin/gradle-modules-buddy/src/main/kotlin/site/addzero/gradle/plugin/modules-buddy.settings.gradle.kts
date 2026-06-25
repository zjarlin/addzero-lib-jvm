package site.addzero.gradle.plugin

import org.gradle.api.Action
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.Property
import site.addzero.gradle.tool.autoIncludeModules

/**
 * 自动模块插件扩展配置
 */
interface AutoModulesPluginExtension {
    val excludeModules: Property<Array<String>>
}

val extension = extensions.create("autoModules", AutoModulesPluginExtension::class.java).apply {
    excludeModules.convention(emptyArray<String>())
}

// 延迟到 settings.gradle.kts 配置完成后再读取扩展，保证 autoModules { ... } 生效
gradle.settingsEvaluated(Action<Settings> {
    autoIncludeModules(*extension.excludeModules.get())
})

package site.addzero.gradle.plugin.assist

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

abstract class AptBuddyExtension {
    abstract val mustMap: MapProperty<String, String>
    abstract val aptScriptOutputDir: Property<String>
    abstract val generatePrecompiledScript: Property<Boolean>
    abstract val settingContext: Property<SettingContextConfig>

    init {
        settingContext.convention(SettingContextConfig())
        generatePrecompiledScript.convention(false)
    }
}

data class SettingContextConfig(
    val contextClassName: String = "SettingContext",
    val settingsClassName: String = "Settings",
    val packageName: String = "site.addzero.context",
    val outputDir: String = "src/main/java",
    val enabled: Boolean = true
)

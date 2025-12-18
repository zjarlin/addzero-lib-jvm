package site.addzero.gradle.plugin

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

abstract class AptBuddyExtension {
    abstract val mustMap: MapProperty<String, String>
    abstract val aptScriptOutputDir: Property<String>
    abstract val generatePrecompiledScript: Property<Boolean>

    // Setting context properties are now directly in the extension
    abstract val contextClassName: Property<String>
    abstract val settingsClassName: Property<String>
    abstract val packageName: Property<String>
    abstract val outputDir: Property<String>
    abstract val settingContextEnabled: Property<Boolean>

    init {
        // Set default values
        contextClassName.convention("SettingContext")
        settingsClassName.convention("Settings")
        packageName.convention("site.addzero.context")
        outputDir.convention("src/main/java")
        settingContextEnabled.convention(true)
        generatePrecompiledScript.convention(false)
    }
}
package site.addzero.gradle.plugin

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

abstract class ProcessorBuddyExtension {
  abstract val mustMap: MapProperty<String, String>

  abstract val interfaceName: Property<String>

  abstract val objectName: Property<String>

  abstract val packageName: Property<String>

  abstract val settingContextEnabled: Property<Boolean>

  abstract val settingsObjectEnabled: Property<Boolean>

  abstract val readmeEnabled: Property<Boolean>

  init {
    interfaceName.convention("SettingContext")
    objectName.convention("Settings")
    packageName.convention("site.addzero.context")
    settingContextEnabled.convention(true)
    settingsObjectEnabled.convention(true)
    readmeEnabled.convention(false)
  }
}

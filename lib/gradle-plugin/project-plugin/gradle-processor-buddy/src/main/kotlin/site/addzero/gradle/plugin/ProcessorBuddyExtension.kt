package site.addzero.gradle.plugin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
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

  abstract val consumerKspBuildLogicEnabled: Property<Boolean>

  abstract val consumerKspBuildLogicOutputDir: DirectoryProperty

  abstract val consumerKspBuildLogicPackageName: Property<String>

  abstract val consumerKspBuildLogicScriptName: Property<String>

  abstract val consumerKspBuildLogicExtensionName: Property<String>

  abstract val consumerKspBuildLogicProcessorProjectPath: Property<String>

  abstract val consumerKspBuildLogicProcessorArtifactId: Property<String>

  abstract val consumerKspBuildLogicProcessorArtifactKind: Property<String>

  abstract val consumerKspBuildLogicCompanionDependencies: ListProperty<String>

  init {
    interfaceName.convention("SettingContext")
    objectName.convention("Settings")
    packageName.convention("site.addzero.context")
    settingContextEnabled.convention(true)
    settingsObjectEnabled.convention(true)
    readmeEnabled.convention(false)
    consumerKspBuildLogicEnabled.convention(false)
    consumerKspBuildLogicPackageName.convention("site.addzero.ksp")
    consumerKspBuildLogicScriptName.convention("")
    consumerKspBuildLogicExtensionName.convention("")
    consumerKspBuildLogicProcessorProjectPath.convention("")
    consumerKspBuildLogicProcessorArtifactId.convention("")
    consumerKspBuildLogicProcessorArtifactKind.convention("")
    consumerKspBuildLogicCompanionDependencies.convention(emptyList())
  }
}

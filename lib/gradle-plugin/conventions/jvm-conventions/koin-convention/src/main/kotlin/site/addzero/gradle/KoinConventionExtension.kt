package site.addzero.gradle

import org.gradle.api.provider.Property

abstract class KoinConventionExtension {
  abstract val bomVersion: Property<String>
  abstract val coreVersion: Property<String>
  abstract val annotationsVersion: Property<String>
  abstract val kspCompilerVersion: Property<String>
  abstract val toolKoinVersion: Property<String>

  init {
    bomVersion.convention("4.1.1")
    coreVersion.convention("4.2.0-beta2")
    annotationsVersion.convention("2.3.2-Beta1")
    kspCompilerVersion.convention("2.3.2-Beta1")
    toolKoinVersion.convention("2025.12.30")
  }
}

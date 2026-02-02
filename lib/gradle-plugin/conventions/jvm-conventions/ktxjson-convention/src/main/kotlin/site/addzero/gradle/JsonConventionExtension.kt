package site.addzero.gradle

import org.gradle.api.provider.Property

abstract class JsonConventionExtension {
  abstract val kotlinxSerializationVersion: Property<String>
  abstract val toolJsonVersion: Property<String>

  init {
    kotlinxSerializationVersion.convention("1.9.0")
    toolJsonVersion.convention("2025.09.30")
  }
}

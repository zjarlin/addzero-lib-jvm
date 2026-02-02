package site.addzero.gradle

import org.gradle.api.provider.Property

abstract class KspConventionExtension {
  /**
   * Version of `com.google.devtools.ksp:symbol-processing-api`.
   */
  abstract val symbolProcessingApiVersion: Property<String>

  init {
    symbolProcessingApiVersion.convention("2.3.5")
  }
}

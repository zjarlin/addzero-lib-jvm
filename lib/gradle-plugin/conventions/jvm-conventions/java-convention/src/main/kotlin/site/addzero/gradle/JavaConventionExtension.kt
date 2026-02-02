package site.addzero.gradle

import org.gradle.api.provider.Property

/**
 * Shared settings for the Java/Kotlin convention plugins so published
 * consumers can override versions without relying on a version catalog.
 */
abstract class JavaConventionExtension {
  /**
   * JDK version used for compilation and toolchains. Defaults to 8
   * to match the previous version catalog entry.
   */
  abstract val jdkVersion: Property<String>

  /**
   * JUnit Jupiter version used for the testing dependencies.
   */
  abstract val junitVersion: Property<String>

  /**
   * Lombok artifact version for the Lombok convention plugin.
   */
  abstract val lombokVersion: Property<String>

  init {
    jdkVersion.convention("8")
    junitVersion.convention("5.8.1")
    lombokVersion.convention("1.18.24")
  }
}

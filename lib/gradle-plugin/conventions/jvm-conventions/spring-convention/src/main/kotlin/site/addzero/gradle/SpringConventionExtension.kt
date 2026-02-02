package site.addzero.gradle

import org.gradle.api.provider.Property

abstract class SpringConventionExtension {
  abstract val springBootVersion: Property<String>
  abstract val junitVersion: Property<String>
  abstract val h2Version: Property<String>

  init {
    springBootVersion.convention("2.7.0")
    junitVersion.convention("5.8.1")
    h2Version.convention("2.3.232")
  }
}

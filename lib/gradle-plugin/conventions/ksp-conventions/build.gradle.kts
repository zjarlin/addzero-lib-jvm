plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation(libs.gradle.plugin.ksp)
  implementation(libs.gradlePlugin.kotlin)
  implementation(libs.tool.yml)
  implementation("site.addzero:tool-gradle-projectdir:2026.01.27")
  gradleApi()
}

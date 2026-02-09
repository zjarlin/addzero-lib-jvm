plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation(libs.com.google.devtools.ksp.gradle.plugin)
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.tool.yml)
  implementation("site.addzero:tool-gradle-projectdir:2026.01.27")
  gradleApi()
}

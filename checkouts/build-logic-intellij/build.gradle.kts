plugins {
  `kotlin-dsl`
}
repositories {
  mavenLocal()
  mavenCentral()
  google()
  gradlePluginPortal()
}
dependencies {
//  implementation(libs.gradlePlugin.dokka)
  implementation(libs.gradlePlugin.kotlin)
  /////////////////intellij///////////////
  implementation(libs.org.jetbrains.intellij.platform.gradle.plugin)
  implementation(libs.org.jetbrains.changelog.gradle.plugin)
  implementation(libs.org.jetbrains.intellij.platform.migration.gradle.plugin)
  implementation(libs.org.jetbrains.intellij.platform.settings.gradle.plugin)
  implementation(libs.org.jetbrains.intellij.platform.base.gradle.plugin)
  implementation(libs.org.jetbrains.intellij.platform.module.gradle.plugin)


}

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
  gradleApi()
  compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
  // 关键：添加 Guice 依赖，解决 Sisu 模块的父类解析问题
  implementation(libs.guice) // 稳定兼容版本
  ////////////jvm/////////////////////
  implementation(libs.addzero.gradle.tool)
  implementation(libs.com.diffplug.spotless.com.diffplug.spotless.gradle.plugin)

  implementation(libs.gradlePlugin.dokka)

  implementation(libs.gradlePlugin.kotlin)
  implementation(libs.org.graalvm.buildtools.native.gradle.plugin)

  ////////////////spring//////////////
  implementation(libs.gradlePlugin.dependencyManagement)
  implementation(libs.gradlePlugin.springBoot)
  implementation(libs.gradlePlugin.kotlinSpring)

  /////////////////intellij///////////////
  implementation(libs.org.jetbrains.intellij.platform.gradle.plugin)
  implementation(libs.org.jetbrains.changelog.gradle.plugin)
  implementation(libs.org.jetbrains.intellij.platform.migration.gradle.plugin)
  implementation(libs.org.jetbrains.intellij.platform.settings.gradle.plugin)
  implementation(libs.org.jetbrains.intellij.platform.base.gradle.plugin)
  implementation(libs.org.jetbrains.intellij.platform.module.gradle.plugin)
  /////////////////kmp///////////////
  implementation(libs.gradlePlugin.jetbrainsCompose)
  implementation(libs.gradlePlugin.composeCompiler)
  implementation(libs.gradlePlugin.kotlinSerialization)

  implementation(libs.gradlePlugin.buildkonfig)
  implementation(libs.gradlePlugin.buildkonfig.cp)

  implementation(libs.gradlePlugin.ktorfit)
  implementation(libs.gradlePlugin.android)
  ///////////////ksp///////////
  implementation(libs.gradle.plugin.ksp)

  implementation(libs.tool.yml)
  implementation("site.addzero:tool-gradle-projectdir:2026.01.27")


//    implementation(project(":checkouts:intellij-convention"))

//    implementation(project(":checkouts:build-logic-jvm"))
}

package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import site.addzero.gradle.tool.configureJUnitPlatform

plugins {
  kotlin("jvm")
  id("site.addzero.gradle.plugin.java-convention")
}
val libs = the<LibrariesForLibs>()
kotlin {
  compilerOptions {
    freeCompilerArgs.set(listOf("-Xjsr305=strict", "-Xjvm-default=all"))
    jvmTarget.set(provider { JvmTarget.fromTarget(libs.versions.jdk.get()) })
  }
  jvmToolchain(libs.versions.jdk.get().toInt())
}
configureJUnitPlatform()


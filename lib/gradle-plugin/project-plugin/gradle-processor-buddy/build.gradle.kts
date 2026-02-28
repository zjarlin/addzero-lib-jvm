import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.`java-gradle-plugin`
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.`kotlin-dsl`
import site.addzero.gradle.tool.configureJ8

buildscript {
  repositories {
    mavenLocal()
    mavenCentral()
  }
  dependencies {
    classpath(libs.site.addzero.gradle.tool.config.java)
  }
}
configureJ8("8")

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

version = "2026.03.02"
dependencies {
  gradleApi()
  implementation(libs.org.jetbrains.kotlin.kotlin.gradle.plugin)
  implementation(libs.site.addzero.tool.str)
  implementation(libs.cn.hutool.hutool.core)
  testImplementation(kotlin("test"))
}

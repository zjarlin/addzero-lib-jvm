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

version="2026.02.24"
dependencies {
  gradleApi()
  implementation(libs.org.jetbrains.kotlin.kotlin.gradle.plugin)
//  implementation(libs.site.addzero.tool.str)
  testImplementation(kotlin("test"))
}

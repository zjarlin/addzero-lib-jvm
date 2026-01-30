import site.addzero.gradle.tool.configureJ8

buildscript {
  repositories {
    mavenLocal()
    mavenCentral()
  }
  dependencies {
    classpath(libs.gradle.tool.config.java)
  }
}
configureJ8("17")

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
  implementation(libs.gradle.plugin.ksp)
  implementation(libs.kotlin.convention)
  implementation(libs.ksp.convention)
}

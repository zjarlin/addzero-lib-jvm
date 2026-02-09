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
configureJ8("8")

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
  implementation(libs.kotlin.convention)
   ////////////////spring//////////////
  implementation(libs.dependency.management.plugin)
  implementation(libs.org.springframework.boot.org.springframework.boot.gradle.plugin)
  implementation(libs.org.jetbrains.kotlin.plugin.spring.gradle.plugin)

}

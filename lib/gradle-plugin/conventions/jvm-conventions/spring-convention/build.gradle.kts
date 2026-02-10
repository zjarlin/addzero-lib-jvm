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

dependencies {
  compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
  implementation(libs.site.addzero.kotlin.convention)
   ////////////////spring//////////////
  implementation(libs.io.spring.gradle.dependency.management.plugin)
  implementation(libs.org.springframework.boot.org.springframework.boot.gradle.plugin.v2)
  implementation(libs.org.jetbrains.kotlin.plugin.spring.org.jetbrains.kotlin.plugin.spring.gradle.plugin)

}

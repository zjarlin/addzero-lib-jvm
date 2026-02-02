package site.addzero.gradle.plugin

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import site.addzero.gradle.JavaConventionExtension
import site.addzero.gradle.tool.configureJUnitPlatform

plugins {
  kotlin("jvm")
  id("site.addzero.gradle.plugin.java-convention")
}

val javaConvention = the<JavaConventionExtension>()
kotlin {
  compilerOptions {
    freeCompilerArgs.set(listOf("-Xjsr305=strict", "-Xjvm-default=all"))
    jvmTarget.set(javaConvention.jdkVersion.map { JvmTarget.fromTarget(it) })
  }
  jvmToolchain(javaConvention.jdkVersion.get().toInt())

}


configureJUnitPlatform()

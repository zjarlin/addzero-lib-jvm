package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import site.addzero.gradle.tool.configureJUnitPlatform
import site.addzero.gradle.tool.configureKotlinTestDependencies

plugins {
    id("site.addzero.gradle.plugin.java-convention")
}
val libs = the<LibrariesForLibs>()
val javaVersion = libs.versions.jdk.get()
configureKotlinCompatibility()
configureKotlinToolchain(javaVersion)

configureKotlinTestDependencies()
configureJUnitPlatform()

fun Project.configureKotlinCompatibility() {
    val the = the<JavaPluginExtension>()
    val toString = the.targetCompatibility.toString()
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.set(listOf("-Xjsr305=strict", "-Xjvm-default=all"))
            jvmTarget.set(provider { JvmTarget.fromTarget(toString) })
        }
    }
}


fun Project.configureKotlinToolchain(jdkVersion: String) {
    val toInt = jdkVersion.toInt()
    val the = the<KotlinJvmProjectExtension>()
    the.jvmToolchain(toInt)
}

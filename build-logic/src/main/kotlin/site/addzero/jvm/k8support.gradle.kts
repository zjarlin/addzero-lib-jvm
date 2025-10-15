
package site.addzero.jvm
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
}

val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xjsr305=strict", "-Xjvm-default=all"))
        jvmTarget.set(provider { JvmTarget.fromTarget(java.targetCompatibility.toString()) })
    }
}
kotlin {
    val jdkversion = libs.versions.jdk.get().toInt()
    jvmToolchain(jdkversion)
}

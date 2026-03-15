package site.addzero.buildlogic.jvm
import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("plugin.serialization")
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-serialization-json").get())
}

package site.addzero.buildlogic.jvm
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.the

import org.gradle.accessors.dm.LibrariesForLibs
plugins {
    kotlin("plugin.serialization")
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = the<LibrariesForLibs>()

dependencies {
    implementation(libs.kotlinx.serialization)
}

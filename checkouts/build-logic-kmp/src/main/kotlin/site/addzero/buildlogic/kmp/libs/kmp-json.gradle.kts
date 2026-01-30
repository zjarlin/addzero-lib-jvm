package site.addzero.buildlogic.kmp.libs

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.the

plugins {
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlin.multiplatform")

}
val libs = the<LibrariesForLibs>()

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization)

        }
    }

}

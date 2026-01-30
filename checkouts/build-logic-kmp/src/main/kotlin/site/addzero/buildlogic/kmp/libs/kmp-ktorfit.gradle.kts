package site.addzero.buildlogic.kmp.libs

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("de.jensklingenberg.ktorfit")
}
val libs = the<LibrariesForLibs>()

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktorfit.lib)
        }

    }
}

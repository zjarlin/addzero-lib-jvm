import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("org.jetbrains.kotlin.multiplatform")
//    kotlin("plugin.serialization")
}
val libs = the<LibrariesForLibs>()

kotlin {
    jvm()
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ksp.symbol.processing.api)

        }
    }
}



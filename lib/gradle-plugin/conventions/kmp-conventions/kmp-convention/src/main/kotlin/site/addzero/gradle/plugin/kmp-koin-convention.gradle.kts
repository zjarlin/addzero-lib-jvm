package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.google.devtools.ksp")
}

val libs = the<LibrariesForLibs>()

dependencies {
    kspCommonMainMetadata(libs.io.insert.koin.koin.ksp.compiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.io.insert.koin.koin.bom))
            implementation(libs.io.insert.koin.koin.annotations)
            implementation(libs.io.insert.koin.koin.core)
            implementation(libs.io.insert.koin.koin.compose)
            implementation(libs.io.insert.koin.koin.compose.viewmodel)
            implementation(libs.io.insert.koin.koin.compose.viewmodel.navigation)
        }
    }
}

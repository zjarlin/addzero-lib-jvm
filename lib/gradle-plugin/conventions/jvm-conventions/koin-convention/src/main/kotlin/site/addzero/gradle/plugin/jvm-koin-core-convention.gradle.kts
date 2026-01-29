package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the

plugins {
    id("site.addzero.gradle.plugin.kotlin-convention")
    id("com.google.devtools.ksp")
}

val libs = the<LibrariesForLibs>()

dependencies {
    ksp(libs.koin.ksp.compiler)
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.annotations)
    implementation(libs.koin.core)
    implementation(libs.tool.koin)
}

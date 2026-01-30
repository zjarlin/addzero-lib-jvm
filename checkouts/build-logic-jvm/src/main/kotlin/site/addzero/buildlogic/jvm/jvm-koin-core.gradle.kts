package site.addzero.buildlogic.jvm
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.the

import org.gradle.accessors.dm.LibrariesForLibs
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

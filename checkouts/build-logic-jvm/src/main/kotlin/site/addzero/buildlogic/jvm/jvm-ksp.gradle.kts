package site.addzero.buildlogic.jvm
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the

plugins {
    id("site.addzero.gradle.plugin.kotlin-convention")
}
val libs = the<LibrariesForLibs>()
kotlin {
    dependencies {
        implementation(libs.ksp.symbol.processing.api)
    }
}

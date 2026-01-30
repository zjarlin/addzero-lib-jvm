package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the
import site.addzero.gradle.*

plugins {
    id("site.addzero.gradle.plugin.java-convention")
}

val javaConvention = the<JavaConventionExtension>()
val libs = the<LibrariesForLibs>()

afterEvaluate {
    dependencies {
        implementation(libs.lombok)
        annotationProcessor(libs.lombok)
    }
}

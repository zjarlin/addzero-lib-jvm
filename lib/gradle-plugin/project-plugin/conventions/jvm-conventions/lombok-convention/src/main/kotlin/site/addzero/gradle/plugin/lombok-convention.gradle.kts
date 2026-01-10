package site.addzero.gradle.plugin

import site.addzero.gradle.*

plugins {
    id("site.addzero.gradle.plugin.java-convention")
}

val javaConvention = the<JavaConventionExtension>()

afterEvaluate {
    dependencies {
        implementation(libs.lombok)
        annotationProcessor(libs.lombok)
    }
}

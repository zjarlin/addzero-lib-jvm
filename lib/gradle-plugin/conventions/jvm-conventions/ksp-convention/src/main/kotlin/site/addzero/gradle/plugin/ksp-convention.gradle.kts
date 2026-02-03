package site.addzero.gradle.plugin

import org.gradle.kotlin.dsl.dependencies
import site.addzero.gradle.KspConventionExtension

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

val kspConvention = extensions.create("kspConvention", KspConventionExtension::class.java)

afterEvaluate {
    kotlin {
        dependencies {
            val version = kspConvention.symbolProcessingApiVersion.get()
            add("implementation", "com.google.devtools.ksp:symbol-processing-api:$version")
        }
    }
}

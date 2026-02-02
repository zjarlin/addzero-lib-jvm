package site.addzero.gradle.plugin

import org.gradle.kotlin.dsl.dependencies
import site.addzero.gradle.JsonConventionExtension

plugins {
    kotlin("plugin.serialization")
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}

val jsonConvention = extensions.create("jsonConvention", JsonConventionExtension::class.java)

afterEvaluate {
    dependencies {
        add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-json:${jsonConvention.kotlinxSerializationVersion.get()}")
    }
}

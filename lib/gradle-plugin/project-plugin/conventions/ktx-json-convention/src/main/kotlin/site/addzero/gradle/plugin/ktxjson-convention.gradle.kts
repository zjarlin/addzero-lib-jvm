package site.addzero.gradle.plugin

import site.addzero.gradle.KtxJsonConventionExtension

plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}

val ktxJson = extensions.create("ktxJson", KtxJsonConventionExtension::class.java)

afterEvaluate {
    val version = ktxJson.version.get()
    val toolVersion = ktxJson.toolVersion.get()
    dependencies {
        add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-json:$version")
        add("implementation", "site.addzero:tool-json:$toolVersion")
    }
}
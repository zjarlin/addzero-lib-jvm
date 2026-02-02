package site.addzero.gradle.plugin

import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import site.addzero.gradle.JsonConventionExtension

plugins {
    id("site.addzero.gradle.plugin.json-convention")
}

val jsonConvention = the<JsonConventionExtension>()

afterEvaluate {
    dependencies {
        add("implementation", "site.addzero:tool-json:${jsonConvention.toolJsonVersion.get()}")
    }
}

package site.addzero.gradle.plugin

import org.gradle.kotlin.dsl.dependencies
import site.addzero.gradle.KoinConventionExtension

plugins {
  id("site.addzero.gradle.plugin.kspplugin-convention")
}

val koinConvention = extensions.create("koinConvention", KoinConventionExtension::class.java)

afterEvaluate {
  dependencies {
    add("ksp", "io.insert-koin:koin-ksp-compiler:${koinConvention.kspCompilerVersion.get()}")
    add("implementation", platform("io.insert-koin:koin-bom:${koinConvention.bomVersion.get()}"))
    add("implementation", "io.insert-koin:koin-core:${koinConvention.coreVersion.get()}")
    add("implementation", "io.insert-koin:koin-annotations:${koinConvention.annotationsVersion.get()}")
    add("implementation", "site.addzero:tool-koin:${koinConvention.toolKoinVersion.get()}")
  }
}

ksp {
  arg("KOIN_DEFAULT_MODULE", "true")
}

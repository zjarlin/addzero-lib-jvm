package site.addzero.gradle.plugin

import org.gradle.kotlin.dsl.dependencies
import site.addzero.gradle.KoinConventionExtension

plugins {
    id("site.addzero.gradle.plugin.kspplugin-convention")
}

val extension = extensions.create<KoinConventionExtension>("koinConvention")

afterEvaluate {
    dependencies {
        ksp("io.insert-koin:koin-ksp-compiler:${extension.kspVersion.get()}")
        implementation(platform("io.insert-koin:koin-bom:${extension.koinBomVersion.get()}"))
        implementation("io.insert-koin:koin-core:${extension.koinBomVersion.get()}")
        implementation("io.insert-koin:koin-annotations:${extension.koinAnnotationsVersion.get()}")
        implementation("site.addzero:tool-koin:${extension.toolKoinVersion.get()}")
    }

}
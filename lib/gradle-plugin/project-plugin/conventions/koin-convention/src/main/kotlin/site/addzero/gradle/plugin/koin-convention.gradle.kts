package site.addzero.buildlogic.jvm

import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.dependencies
import site.addzero.gradle.KoinConventionExtension

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("site.addzero.buildlogic.jvm.kspapi-convention")
}

val extension = the<KoinConventionExtension>()

dependencies {
    ksp("io.insert-koin:koin-ksp-compiler:${extension.kspVersion.get()}")
    implementation(project.dependencies.platform("io.insert-koin:koin-bom:${extension.koinBomVersion.get()}"))
    implementation("io.insert-koin:koin-annotations:${extension.koinAnnotationsVersion.get()}")
    implementation("io.insert-koin:koin-core:${extension.koinCoreVersion.get()}")
    implementation("site.addzero:tool-koin:${extension.toolKoinVersion.get()}")
}
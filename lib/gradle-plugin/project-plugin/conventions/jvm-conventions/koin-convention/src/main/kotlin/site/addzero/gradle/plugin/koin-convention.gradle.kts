package site.addzero.gradle.plugin

import org.gradle.kotlin.dsl.dependencies
import site.addzero.gradle.KoinConventionExtension

plugins {
    id("site.addzero.gradle.plugin.kspplugin-convention")
}

val extension = extensions.create<KoinConventionExtension>("koinConvention")

afterEvaluate {
    dependencies {
        ksp(libs.koin.ksp.compiler)
        implementation(platform("io.insert-koin:koin-bom:${extension.koinBomVersion.get()}"))
        implementation(libs.koin.core)
        implementation(libs.koin.annotations)
        implementation(libs.tool.koin)
    }

}
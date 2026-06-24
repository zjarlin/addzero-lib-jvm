@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
val libs = versionCatalogs.named("libs")

kotlin{
    dependencies {
        implementation(project(":lib:ksp:metadata:gen-reified:gen-reified-core"))
        implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
    }
}

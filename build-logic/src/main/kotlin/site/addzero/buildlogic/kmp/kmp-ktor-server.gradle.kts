package site.addzero.buildlogic.kmp

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("io.ktor.plugin")
    application
}

val libs = versionCatalogs.named("libs")

group = "site.addzero.vp1"
version = libs.findVersion("ktor-server-app-version").get().requiredVersion

application {
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

kotlin {
    dependencies {
        implementation(libs.findLibrary("site-addzero-ktor-banner").get())
        implementation(libs.findLibrary("io-ktor-ktor-server-swagger").get())
        implementation(libs.findLibrary("io-ktor-ktor-server-content-negotiation").get())
        implementation(libs.findLibrary("io-ktor-ktor-server-status-pages").get())
        implementation(libs.findLibrary("io-ktor-ktor-serialization-kotlinx-json").get())
        implementation(libs.findLibrary("ch-qos-logback-logback-classic").get())
        implementation(libs.findLibrary("io-ktor-ktor-server-netty-jvm").get())
        implementation(libs.findLibrary("io-ktor-ktor-server-core").get())
        implementation(libs.findLibrary("io-insert-koin-koin-ktor").get())
        testImplementation(libs.findLibrary("ktor-server-test-host").get())
    }
}

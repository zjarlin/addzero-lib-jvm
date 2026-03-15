package site.addzero.buildlogic.kmp

plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("io-ktor-ktor-client-core").get())
            implementation(libs.findLibrary("io-ktor-ktor-client-content-negotiation").get())
            implementation(libs.findLibrary("io-ktor-ktor-serialization-kotlinx-json").get())
            implementation(libs.findLibrary("io-ktor-ktor-client-logging").get())
            implementation(libs.findLibrary("ch-qos-logback-logback-classic").get())
        }
        jvmMain.dependencies {
            api(libs.findLibrary("io-ktor-ktor-client-cio").get())
        }
        wasmJsMain.dependencies {
            api(libs.findLibrary("io-ktor-ktor-client-js").get())
        }
        nativeMain.dependencies {
            api(libs.findLibrary("io-ktor-ktor-client-darwin").get())
        }
    }
}

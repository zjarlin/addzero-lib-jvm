package site.addzero.gradle.plugin


plugins {
    id("site.addzero.gradle.plugin.kmp-convention")
}

kotlin {
    mingwX64()
    jvm()
    wasmJs {
        nodejs()
    }
}
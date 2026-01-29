package site.addzero.gradle.plugin

plugins {
    id("site.addzero.gradle.plugin.kmp-convention")
}

kotlin {
    wasmJs {
//        nodejs()
        browser()
    }
}

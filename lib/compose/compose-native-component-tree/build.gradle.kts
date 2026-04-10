plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.findLibrary("compose-apple-corner").get())
                implementation(libs.findLibrary("site-addzero-compose-native-component-button").get())
                implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
                implementation(libs.findLibrary("tool-tree").get())
            }
        }
    }
}

version="2026.04.11"

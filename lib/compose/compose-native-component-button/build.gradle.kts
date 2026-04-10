plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.findLibrary("site-addzero-compose-native-component-high-level").get())
            }
        }
    }
}

version="2026.04.11"

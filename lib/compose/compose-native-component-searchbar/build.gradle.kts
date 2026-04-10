import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain {
            dependencies {

                implementation(libs.findLibrary("site-addzero-compose-native-component-button").get())

            }
        }
    }

}
version="2026.04.11"



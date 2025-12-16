plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "2025.12.20"
}

dependencies {
    implementation("site.addzero:tool-curl:0.0.672")
    implementation(libs.okhttp)
    implementation(libs.jackson.module.kotlin)
}

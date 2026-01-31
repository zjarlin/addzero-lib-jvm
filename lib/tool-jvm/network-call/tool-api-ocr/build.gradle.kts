plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+" version "2025.12.20"
}

dependencies {
    implementation(libs.tool.curl)
    implementation(libs.okhttp)
    implementation(libs.jackson.module.kotlin)
}

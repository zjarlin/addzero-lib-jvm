plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")  version "2025.12.20"
}

dependencies {
    implementation(libs.tool.curl)
    implementation(libs.okhttp)
    implementation(libs.jackson.module.kotlin)
}

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")  version "2025.12.20"
}

dependencies {
    implementation(libs.site.addzero.tool.curl)
    implementation(libs.com.squareup.okhttp3.okhttp)
    implementation(libs.com.fasterxml.jackson.module.jackson.module.kotlin)
}

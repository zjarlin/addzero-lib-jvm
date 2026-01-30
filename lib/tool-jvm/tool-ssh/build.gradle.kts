plugins {
    id("site.addzero.gradle.plugin.kotlin-convention")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.sshj)
    implementation(libs.slf4j.api)
}

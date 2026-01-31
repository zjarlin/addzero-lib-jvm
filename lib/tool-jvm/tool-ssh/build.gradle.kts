plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.sshj)
    implementation(libs.slf4j.api)
}

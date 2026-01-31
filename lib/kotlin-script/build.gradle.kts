plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}

dependencies {
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
}

description = "Kotlin script template utilities"

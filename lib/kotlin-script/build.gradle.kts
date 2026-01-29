plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
}

description = "Kotlin script template utilities"

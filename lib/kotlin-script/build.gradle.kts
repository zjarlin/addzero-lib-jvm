plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
    testImplementation(libs.org.jetbrains.kotlin.kotlin.test)
    testImplementation(libs.junit.junit.junit.jupiter)
}

description = "Kotlin script template utilities"

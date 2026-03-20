plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

group = "site.addzero"

dependencies {
    compileOnly(libs.org.jetbrains.kotlin.kotlin.compiler.embeddable)
    testImplementation(libs.org.jetbrains.kotlin.kotlin.compiler.embeddable)
    testImplementation(libs.org.junit.jupiter.junit.jupiter)
}

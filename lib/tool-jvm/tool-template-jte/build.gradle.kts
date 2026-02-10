plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    // JTE Template Engine
    implementation(libs.gg.jte.jte)

    // 测试依赖
    testImplementation(libs.org.jetbrains.kotlin.kotlin.test)
    testImplementation(libs.junit.junit.junit.jupiter)
}

description = "JTE Template Engine utilities"

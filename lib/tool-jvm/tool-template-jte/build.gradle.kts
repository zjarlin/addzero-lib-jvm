plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    // JTE Template Engine
    implementation(libs.jte)

    // 测试依赖
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.junit.jupiter)
}

description = "JTE Template Engine utilities"

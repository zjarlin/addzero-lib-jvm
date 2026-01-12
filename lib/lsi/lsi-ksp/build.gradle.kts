plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}

dependencies {
    api(libs.lsi.core)
    implementation(libs.tool.str)
    // KSP API dependencies
    implementation(libs.symbol.processing.api)

//    // 测试依赖
//    testImplementation(libs.junit.jupiter)
//    testImplementation(libs.kotest.runner.junit5)
//    testImplementation(libs.kotest.assertions.core)
//    testImplementation(libs.kotest.property)
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


description = "LSI系统的KSP实现模块"

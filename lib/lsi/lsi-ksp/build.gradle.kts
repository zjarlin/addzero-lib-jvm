plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")  version "+"
}

dependencies {
    api(libs.site.addzero.lsi.core)
    implementation(libs.site.addzero.tool.str)
    // KSP API dependencies
    implementation(libs.com.google.devtools.ksp.symbol.processing.api)

//    // 测试依赖
//    testImplementation(libs.junit.junit.junit.jupiter)
//    testImplementation(libs.io.kotest.kotest.runner.junit5)
//    testImplementation(libs.io.kotest.kotest.assertions.core)
//    testImplementation(libs.io.kotest.kotest.property)
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


description = "LSI系统的KSP实现模块"

plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}

dependencies {
    api(project(":checkouts:lsi:lsi-core"))
    implementation("site.addzero:tool-str:2026.02.23")
    // KSP API dependencies
    implementation(libs.com.google.devtools.ksp.symbol.processing.api)

    // kotlinpoet: LsiClass → ClassName bridge
    compileOnly("com.squareup:kotlinpoet:2.2.0")
    compileOnly("com.squareup:kotlinpoet-ksp:2.2.0")

//    // 测试依赖
//    testImplementation(libs.junit.jupiter)
//    testImplementation(libs.kotest.runner.junit5)
//    testImplementation(libs.kotest.assertions.core)
//    testImplementation(libs.kotest.property)
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


description = "LSI系统的KSP实现模块"

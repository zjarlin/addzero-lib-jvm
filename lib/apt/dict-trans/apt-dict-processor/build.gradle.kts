plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(libs.ksp.symbol.processing.api)
    implementation(libs.kotlinpoet.ksp)
    implementation(libs.hutool.core)
    
    // 引用核心注解模块
    api(project(":lib:apt:apt-dict-annotations"))
    
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
}
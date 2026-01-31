plugins {
//    id("site.addzero.buildlogic.spring.spring-lib-convention")
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
    id("site.addzero.gradle.plugin.kspplugin-convention")
//    id("site.addzero.kcp.reified") version "2026.01.01"

//    id("site.addzero.buildlogic.auto-jvmname")
}

dependencies {
    // KSP reified 处理器
    ksp(libs.gen.reified.processor)
    implementation(libs.gen.reified.core)

//    compileOnly(libs.spring.jdbc)
    compileOnly(libs.spring.context)
//    compileOnly(libs.spring.web)
    compileOnly(libs.spring.webmvc)

//    compileOnly(libs.hutool.all)
//    implementation(libs.jackson.module.kotlin)
//    compileOnly(libs.javax.servlet.api)
}

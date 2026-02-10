plugins {
//    id("site.addzero.buildlogic.spring.spring-lib-convention")
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
    id("site.addzero.gradle.plugin.kspplugin-convention")
//    id("site.addzero.kcp.reified") version "2026.01.01"

//    id("site.addzero.buildlogic.auto-jvmname")
}

dependencies {
    // KSP reified 处理器
    ksp(libs.site.addzero.gen.reified.processor)
    implementation(libs.site.addzero.gen.reified.core)

//    compileOnly(libs.org.springframework.spring.jdbc)
    compileOnly(libs.org.springframework.spring.context)
//    compileOnly(libs.org.springframework.spring.web)
    compileOnly(libs.org.springframework.spring.webmvc)

//    compileOnly(libs.cn.hutool.hutool.all)
//    implementation(libs.com.fasterxml.jackson.module.jackson.module.kotlin)
//    compileOnly(libs.javax.servlet.javax.servlet.api)
}

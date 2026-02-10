plugins {
    id("site.addzero.buildlogic.spring.spring-starter")
}


dependencies {
//    compileOnly(libs.jakarta.servlet.jakarta.servlet.api) // 或使用 Spring Boot 管理的版本
    implementation(libs.cn.hutool.hutool.all)
//    implementation(libs.org.jetbrains.kotlin.kotlin.reflect)
//    implementation(libs.site.addzero.tool.str)
    implementation(libs.com.fasterxml.jackson.module.jackson.module.kotlin)
    implementation(libs.org.springframework.spring.webmvc)
    compileOnly(libs.javax.servlet.javax.servlet.api) // 或使用 Spring Boot 管理的版本
//    implementation(libs.org.springframework.boot.spring.boot.autoconfigure)
    // Spring Boot 管理的版本
//    implementation(projects.lib.toolKmp.tool)
}

plugins {
    id("site.addzero.buildlogic.spring.spring-starter")
}


dependencies {
//    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0") // 或使用 Spring Boot 管理的版本
    implementation(libs.hutool.all)
//    implementation(libs.kotlin.reflect)
//    implementation("site.addzero:addzero-tool-str:2025.09.20")
    implementation(libs.jackson.module.kotlin)
    implementation(libs.spring.webmvc)
    compileOnly(libs.javax.servlet.api) // 或使用 Spring Boot 管理的版本
//    implementation(libs.spring.boot.autoconfigure)
    // Spring Boot 管理的版本
//    implementation(projects.lib.toolKmp.tool)
}

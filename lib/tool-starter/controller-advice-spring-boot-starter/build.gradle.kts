plugins {
    id("site.addzero.gradle.plugin.spring-starter-convention")
}


dependencies {
//    compileOnly(libs.jakarta.servlet.api) // 或使用 Spring Boot 管理的版本
    implementation(libs.hutool.all)
//    implementation(libs.kotlin.reflect)
//    implementation(libs.addzero.tool.str)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.spring.webmvc)
    compileOnly(libs.javax.servlet.api) // 或使用 Spring Boot 管理的版本
//    implementation(libs.spring.boot.autoconfigure)
    // Spring Boot 管理的版本
//    implementation(projects.lib.toolKmp.tool)
}

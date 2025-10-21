plugins {
    id("site.addzero.buildlogic.spring.spring-lib-convention")
}

dependencies {
    implementation(libs.hutool.all)
    implementation(libs.jackson.module.kotlin)
    compileOnly(libs.spring.webmvc)
    compileOnly(libs.javax.servlet.api) // 或使用 Spring Boot 管理的版本
}

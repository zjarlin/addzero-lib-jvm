plugins {
    id("site.addzero.spring.spring-lib-convention")
}

dependencies {
    implementation(libs.hutool.all)
    compileOnly(libs.spring.webmvc)
    compileOnly(libs.javax.servlet.api) // 或使用 Spring Boot 管理的版本
}

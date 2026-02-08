plugins {
    id("site.addzero.buildlogic.spring.spring-starter")
}


dependencies {
//    compileOnly(libs.jakarta.servlet.api) // 或使用 Spring Boot 管理的版本
//    implementation(libs.hutool.all)
    implementation(libs.fastjson2.kotlin)
    implementation(libs.spring.webmvc)
    compileOnly(libs.javax.servlet.api) // 或使用 Spring Boot 管理的版本
//    implementation(libs.spring.boot.autoconfigure)
    api(libs.controller.autoconfigure) // 或使用
// Spring Boot 管理的版本
}


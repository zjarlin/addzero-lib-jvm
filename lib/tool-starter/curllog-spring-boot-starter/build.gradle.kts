plugins {
    id("site.addzero.buildlogic.spring.spring-starter")
}


dependencies {
//    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0") // 或使用 Spring Boot 管理的版本
//    implementation(libs.hutool.all)
    implementation(libs.fastjson2.kotlin)
    implementation(libs.spring.webmvc)
    compileOnly(libs.javax.servlet.api) // 或使用 Spring Boot 管理的版本
//    implementation(libs.spring.boot.autoconfigure)
    api("site.addzero:addzero-controller-autoconfigure:2025.09.29") // 或使用
// Spring Boot 管理的版本
}


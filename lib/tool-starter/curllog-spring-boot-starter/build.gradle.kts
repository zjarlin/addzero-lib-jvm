plugins {
    id("site.addzero.buildlogic.spring.spring-starter")
}


dependencies {
//    compileOnly(libs.jakarta.servlet.jakarta.servlet.api) // 或使用 Spring Boot 管理的版本
//    implementation(libs.cn.hutool.hutool.all)
    implementation(libs.com.alibaba.fastjson2.fastjson2.kotlin)
    implementation(libs.org.springframework.spring.webmvc)
    compileOnly(libs.javax.servlet.javax.servlet.api) // 或使用 Spring Boot 管理的版本
//    implementation(libs.org.springframework.boot.spring.boot.autoconfigure)
    api(libs.site.addzero.controller.autoconfigure) // 或使用
// Spring Boot 管理的版本
}


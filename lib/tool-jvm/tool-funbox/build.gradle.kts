plugins {
    id("site.addzero.buildlogic.spring.spring-lib-convention")
}

dependencies {
    implementation(libs.hutool.all)
//    implementation(libs.jackson.module.kotlin)
    compileOnly(libs.fastjson2.kotlin)
    implementation(libs.spring.webmvc)
    compileOnly(libs.javax.servlet.api) // 或使用 Spring Boot 管理的版本
    implementation("io.swagger:swagger-annotations:1.6.12")
    implementation("site.addzero:tool-reflection:0.0.672")
}

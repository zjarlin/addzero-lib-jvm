plugins {
    id("spring-starter")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

kotlin {
    jvmToolchain(8)
}

dependencies {
//    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0") // 或使用 Spring Boot 管理的版本
    implementation(libs.hutool.all)
//    implementation(libs.kotlin.reflect)
//    implementation("site.addzero:addzero-tool-str:2025.09.20")
    implementation(libs.jackson.module.kotlin)
    implementation(libs.fastjson2.kotlin)
    implementation(libs.spring.webmvc)
    compileOnly(libs.javax.servlet.api) // 或使用 Spring Boot 管理的版本
//    implementation(libs.spring.boot.autoconfigure)
    api(projects.lib.toolSpring.starter.controllerAutoconfigure) // 或使用
    // Spring Boot 管理的版本
    implementation(projects.lib.toolKmp.tool)
}

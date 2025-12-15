
plugins {
    id("site.addzero.buildlogic.spring.spring-starter")
}
dependencies {
    // AOP 支持
    compileOnly("org.aspectj:aspectjweaver")

    implementation(libs.hutool.all)
    implementation(libs.fastjson2.kotlin)
    implementation("site.addzero:tool-reflection:2025.10.20")
    implementation(libs.byte.buddy)
    implementation(libs.slf4j.api)
    api("site.addzero:dict-trans-core:2025.10.20")
    // 缓存依赖
    implementation(libs.caffeine)
    // 添加Spring Boot核心依赖
    implementation(libs.spring.boot.autoconfigure)
    // 添加Spring Boot配置处理器依赖
//    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
//    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // 配置处理器 - 重要！
//    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
//    implementation("org.springframework.boot:spring-boot-configuration-processor")
    // Spring Boot 自动配置核心依赖
// Spring Boot
// 管理的版本
}

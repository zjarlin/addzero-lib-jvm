plugins {
    id("site.addzero.buildlogic.spring.spring-starter")
}
dependencies {
    implementation(libs.hutool.all)
    implementation(libs.fastjson2.kotlin)
    implementation(projects.lib.toolJvm.toolReflection)
    implementation(libs.byte.buddy)
    api(projects.lib.toolStarter.dictTransCore) // 或使用
    // 配置处理器 - 重要！
//    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
//    implementation("org.springframework.boot:spring-boot-configuration-processor")
    // Spring Boot 自动配置核心依赖


//    kapt("org.springframework.boot:spring-boot-configuration-processor")
// Spring Boot
// 管理的版本
}




plugins {
    id("spring-starter")
}
dependencies {
    api(projects.lib.toolSpring.starter.dictTransCore)
    implementation(libs.hutool.all)
    implementation(libs.fastjson2.kotlin)
    implementation(libs.byte.buddy)
//    api(projects.lib.toolSpring. starter.controllerAutoconfigure) // 或使用
     // 配置处理器 - 重要！
//    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
//    implementation("org.springframework.boot:spring-boot-configuration-processor")
       // Spring Boot 自动配置核心依赖


//    kapt("org.springframework.boot:spring-boot-configuration-processor")
// Spring Boot
// 管理的版本
}




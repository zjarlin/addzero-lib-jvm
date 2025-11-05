plugins {
    id("site.addzero.buildlogic.spring.spring-starter")
}
dependencies {
    implementation(libs.hutool.all)
    implementation(libs.fastjson2.kotlin)
//    implementation("site.addzero:tool-reflection:${libs.versions.addzero.lib.get()}")
    implementation("site.addzero:tool-reflection:0.0.673")

    implementation(libs.byte.buddy)
    api("site.addzero:dict-trans-core:${libs.versions.addzero.lib.get()}")
    // 配置处理器 - 重要！
//    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
//    implementation("org.springframework.boot:spring-boot-configuration-processor")
    // Spring Boot 自动配置核心依赖


//    kapt("org.springframework.boot:spring-boot-configuration-processor")
// Spring Boot
// 管理的版本
}




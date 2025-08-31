plugins {
    id("spring-starter")
}
dependencies {
    api(projects.lib.toolSpring.starter.addzeroDictTransCore)
    implementation(libs.hutool.all)
    implementation(libs.fastjson2.kotlin)
    implementation(libs.byte.buddy)
    api(projects.lib.toolSpring. starter.addzeroControllerAutoconfigure) // 或使用
// Spring Boot
// 管理的版本
}




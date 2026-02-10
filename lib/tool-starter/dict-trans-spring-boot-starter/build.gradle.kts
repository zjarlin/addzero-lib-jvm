plugins {
  id("site.addzero.buildlogic.spring.spring-starter")
}
dependencies {
//    implementation(libs.cn.hutool.hutool.all)
  implementation(libs.com.alibaba.fastjson2.fastjson2.kotlin)
//    implementation(libs.site.addzero.tool.reflection)
  implementation(libs.site.addzero.tool.reflection)
  implementation(project(":lib:tool-jvm:tool-bean"))
  implementation(libs.site.addzero.tool.bytebuddy)
  implementation(libs.site.addzero.tool.str)

  api(libs.site.addzero.dict.trans.core)

  // 添加Spring Boot核心依赖
//  implementation(libs.org.springframework.boot.spring.boot.autoconfigure)
  implementation("site.addzero:controller-autoconfigure:2026.01.11")
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

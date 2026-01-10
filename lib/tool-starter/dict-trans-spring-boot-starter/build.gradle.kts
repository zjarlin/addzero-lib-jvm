plugins {
  id("site.addzero.buildlogic.spring.spring-starter")
}
dependencies {
//    implementation(libs.hutool.all)
  implementation(libs.fastjson2.kotlin)
//    implementation(libs.tool.reflection)
  implementation(libs.tool.reflection)
  implementation(project(":lib:tool-jvm:tool-bean"))
  implementation(libs.tool.bytebuddy)
  implementation(libs.tool.str)

  api(libs.dict.trans.core)

  // 添加Spring Boot核心依赖
//  implementation(libs.spring.boot.autoconfigure)
  implementation(projects.lib.toolStarter.controllerAutoconfigure)
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

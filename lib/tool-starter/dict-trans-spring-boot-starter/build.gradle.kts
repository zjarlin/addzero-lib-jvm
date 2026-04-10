plugins {
  id("site.addzero.buildlogic.spring.spring-starter")
}
val libs = versionCatalogs.named("libs")

dependencies {
//    implementation(libs.findLibrary("cn-hutool-hutool-all").get())
  implementation(libs.findLibrary("com-alibaba-fastjson2-fastjson2-kotlin").get())
//    implementation(libs.findLibrary("site-addzero-tool-reflection").get())
  implementation(libs.findLibrary("site-addzero-tool-reflection").get())
  implementation(project(":lib:tool-jvm:tool-bean"))
  implementation(libs.findLibrary("site-addzero-tool-bytebuddy").get())
  implementation(libs.findLibrary("site-addzero-tool-str").get())

  api(libs.findLibrary("site-addzero-dict-trans-core").get())

  // 添加Spring Boot核心依赖
//  implementation(libs.org.springframework.boot.spring.boot.autoconfigure)
  implementation(libs.findLibrary("site-addzero-controller-autoconfigure").get())
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

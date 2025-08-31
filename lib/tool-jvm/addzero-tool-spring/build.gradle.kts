plugins {
    id("spring-convention")
}
dependencies {
    implementation(libs.hutool.all)
    compileOnly(libs.spring.webmvc)
    compileOnly(libs.jakarta.servlet.api) // 或使用 Spring Boot 管理的版本

}



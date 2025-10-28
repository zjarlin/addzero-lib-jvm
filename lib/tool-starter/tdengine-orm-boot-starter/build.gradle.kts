plugins {
//    id("site.addzero.buildlogic.spring.spring-starter")
    id("site.addzero.buildlogic.jvm.lombok-convention")
}


dependencies {
//    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0") // 或使用 Spring Boot 管理的版本
//    implementation(libs.hutool.all)
    implementation("org.springframework.boot:spring-boot-autoconfigure:2.4.2")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc:2.4.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation(libs.hutool.all)
//    implementation(libs.spring.boot.autoconfigure)
//    api(projects.lib.toolStarter.controllerAutoconfigure) // 或使用
// Spring Boot 管理的版本
}


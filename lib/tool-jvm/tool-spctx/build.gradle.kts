plugins {
//    id("site.addzero.buildlogic.spring.spring-lib-convention")
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
//    id("site.addzero.kcp.reified") version "2026.01.01"

//    id("site.addzero.buildlogic.auto-jvmname")
}

dependencies {
    // KSP reified 处理器
    ksp("site.addzero:gen-reified-processor:2026.01.08")
    implementation("site.addzero:gen-reified-core:2026.01.01")

//    compileOnly("org.springframework:spring-jdbc:5.3.21")
    compileOnly("org.springframework:spring-context:5.3.21")
//    compileOnly("org.springframework:spring-web:5.3.21")
    compileOnly("org.springframework:spring-webmvc:5.3.21")

//    compileOnly(libs.hutool.all)
//    implementation(libs.jackson.module.kotlin)
//    compileOnly("javax.servlet:javax.servlet-api:4.0.1")
}

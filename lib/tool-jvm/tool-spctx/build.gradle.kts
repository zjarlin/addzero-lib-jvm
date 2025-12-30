plugins {
//    id("site.addzero.buildlogic.spring.spring-lib-convention")
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
//    id("site.addzero.kcp.reified") version "2026.01.01"

//    id("site.addzero.buildlogic.auto-jvmname")
}

dependencies {
    // KSP reified 处理器
    ksp(project(":lib:kcp:kcp-reified:kcp-reified-ksp"))
    api(project(":lib:kcp:kcp-reified:kcp-reified-annotations"))

//    compileOnly("org.springframework:spring-jdbc:5.3.21")
    compileOnly("org.springframework:spring-context:5.3.21")
//    compileOnly("org.springframework:spring-web:5.3.21")
    compileOnly("org.springframework:spring-webmvc:5.3.21")

//    compileOnly(libs.hutool.all)
//    implementation(libs.jackson.module.kotlin)
//    compileOnly("javax.servlet:javax.servlet-api:4.0.1")
}

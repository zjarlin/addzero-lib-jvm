plugins {
//    id("site.addzero.buildlogic.spring.spring-lib-convention")
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("site.addzero.kcp.reified") version "+"

//    id("site.addzero.buildlogic.auto-jvmname")
}

dependencies {
    // KCP reified 插件
//    kotlinCompilerPluginClasspath(project(":lib:kcp:kcp-reified:kcp-reified-plugin"))
//    api(project(":lib:kcp:kcp-reified:kcp-reified-annotations"))
//    implementation(project(":lib:kcp:kcp-reified:kcp-reified-annotations"))

//    compileOnly("org.springframework:spring-jdbc:5.3.21")
    compileOnly("org.springframework:spring-context:5.3.21")
//    compileOnly("org.springframework:spring-web:5.3.21")
    compileOnly("org.springframework:spring-webmvc:5.3.21")

//    compileOnly(libs.hutool.all)
//    implementation(libs.jackson.module.kotlin)
//    compileOnly("javax.servlet:javax.servlet-api:4.0.1")
}

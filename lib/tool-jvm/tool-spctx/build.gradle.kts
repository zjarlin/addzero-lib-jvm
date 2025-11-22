plugins {
//    id("site.addzero.buildlogic.spring.spring-lib-convention")
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {

//    compileOnly("org.springframework:spring-jdbc:5.3.21")
    compileOnly("org.springframework:spring-context:5.3.21")
//    compileOnly("org.springframework:spring-web:5.3.21")
    compileOnly("org.springframework:spring-webmvc:5.3.21")

//    compileOnly(libs.hutool.all)
//    implementation(libs.jackson.module.kotlin)
//    compileOnly("javax.servlet:javax.servlet-api:4.0.1")
}

plugins {
//    id("site.addzero.buildlogic.spring.spring-lib-convention")
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {

    compileOnly(libs.org.springframework.spring.jdbc)
    compileOnly(libs.org.springframework.spring.context)
    compileOnly(libs.org.springframework.spring.web)
    compileOnly(libs.org.springframework.spring.webmvc)

    implementation(libs.cn.hutool.hutool.all)
    implementation(libs.com.fasterxml.jackson.module.jackson.module.kotlin)
    compileOnly(libs.javax.servlet.javax.servlet.api)
}

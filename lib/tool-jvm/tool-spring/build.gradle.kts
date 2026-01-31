plugins {
//    id("site.addzero.buildlogic.spring.spring-lib-convention")
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}

dependencies {

    compileOnly(libs.spring.jdbc)
    compileOnly(libs.spring.context)
    compileOnly(libs.spring.web)
    compileOnly(libs.spring.webmvc)

    implementation(libs.hutool.all)
    implementation(libs.jackson.module.kotlin)
    compileOnly(libs.javax.servlet.api)
}

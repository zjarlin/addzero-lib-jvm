plugins {
//    id("site.addzero.buildlogic.spring.spring-lib-convention")
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {

//    compileOnly(libs.spring.jdbc)
    compileOnly(libs.spring.context)
//    compileOnly(libs.spring.web)
//    compileOnly(libs.spring.webmvc)
    implementation(libs.spring.expression)


    compileOnly(libs.hutool.extra)
}

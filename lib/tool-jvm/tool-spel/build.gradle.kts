plugins {
//    id("site.addzero.buildlogic.spring.spring-lib-convention")
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies {

//    compileOnly(libs.findLibrary("org-springframework-spring-jdbc").get())
    compileOnly(libs.findLibrary("org-springframework-spring-context").get())
//    compileOnly(libs.findLibrary("org-springframework-spring-web").get())
//    compileOnly(libs.org.springframework.spring.webmvc)
    implementation(libs.findLibrary("org-springframework-spring-expression").get())


    compileOnly(libs.findLibrary("cn-hutool-hutool-extra").get())
}

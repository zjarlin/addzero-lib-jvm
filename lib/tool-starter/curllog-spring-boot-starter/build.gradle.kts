plugins {
    id("site.addzero.buildlogic.spring.spring-starter")
}
val catalogLibs = versionCatalogs.named("libs")


dependencies {
//    compileOnly(catalogLibs.findLibrary("jakarta-servlet-jakarta-servlet-api").get()) // 或使用 Spring Boot 管理的版本
//    implementation(catalogLibs.findLibrary("cn-hutool-hutool-all").get())
    implementation(catalogLibs.findLibrary("com-alibaba-fastjson2-fastjson2-kotlin").get())
    implementation(libs.org.springframework.spring.webmvc)
    compileOnly(catalogLibs.findLibrary("javax-servlet-javax-servlet-api").get()) // 或使用 Spring Boot 管理的版本
//    implementation(libs.org.springframework.boot.spring.boot.autoconfigure)
    api(catalogLibs.findLibrary("site-addzero-controller-autoconfigure").get()) // 或使用
// Spring Boot 管理的版本
}


plugins {
    id("site.addzero.buildlogic.spring.spring-starter")
}
val catalogLibs = versionCatalogs.named("libs")


dependencies {
//    compileOnly(catalogLibs.findLibrary("jakarta-servlet-jakarta-servlet-api").get()) // 或使用 Spring Boot 管理的版本
    implementation(catalogLibs.findLibrary("cn-hutool-hutool-all").get())
//    implementation(libs.org.jetbrains.kotlin.kotlin.reflect)
//    implementation(catalogLibs.findLibrary("site-addzero-tool-str").get())
    implementation(catalogLibs.findLibrary("com-fasterxml-jackson-module-jackson-module-kotlin").get())
    implementation(libs.org.springframework.spring.webmvc)
    compileOnly(catalogLibs.findLibrary("javax-servlet-javax-servlet-api").get()) // 或使用 Spring Boot 管理的版本
//    implementation(libs.org.springframework.boot.spring.boot.autoconfigure)
    // Spring Boot 管理的版本
//    implementation(projects.lib.toolKmp.tool)
}

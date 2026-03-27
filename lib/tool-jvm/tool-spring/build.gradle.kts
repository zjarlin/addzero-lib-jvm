plugins {
//    id("site.addzero.buildlogic.spring.spring-lib-convention")
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val catalogLibs = versionCatalogs.named("libs")

dependencies {

    compileOnly(catalogLibs.findLibrary("org-springframework-spring-jdbc").get())
    compileOnly(catalogLibs.findLibrary("org-springframework-spring-context").get())
    compileOnly(catalogLibs.findLibrary("org-springframework-spring-web").get())
    compileOnly(libs.org.springframework.spring.webmvc)

    implementation(catalogLibs.findLibrary("cn-hutool-hutool-all").get())
    implementation(catalogLibs.findLibrary("com-fasterxml-jackson-module-jackson-module-kotlin").get())
    compileOnly(catalogLibs.findLibrary("javax-servlet-javax-servlet-api").get())
}

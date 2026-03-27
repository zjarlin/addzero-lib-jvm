plugins {
//    id("site.addzero.buildlogic.spring.spring-lib-convention")
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
//    id("site.addzero.kcp.reified") version "2026.01.01"

//    id("site.addzero.buildlogic.auto-jvmname")
}
val catalogLibs = versionCatalogs.named("libs")

dependencies {
    // KSP reified 处理器
    ksp(catalogLibs.findLibrary("site-addzero-gen-reified-processor").get())
    implementation(catalogLibs.findLibrary("site-addzero-gen-reified-core").get())

//    compileOnly(catalogLibs.findLibrary("org-springframework-spring-jdbc").get())
    compileOnly(catalogLibs.findLibrary("org-springframework-spring-context").get())
//    compileOnly(catalogLibs.findLibrary("org-springframework-spring-web").get())
    compileOnly(libs.org.springframework.spring.webmvc)

//    compileOnly(catalogLibs.findLibrary("cn-hutool-hutool-all").get())
//    implementation(catalogLibs.findLibrary("com-fasterxml-jackson-module-jackson-module-kotlin").get())
//    compileOnly(catalogLibs.findLibrary("javax-servlet-javax-servlet-api").get())
}

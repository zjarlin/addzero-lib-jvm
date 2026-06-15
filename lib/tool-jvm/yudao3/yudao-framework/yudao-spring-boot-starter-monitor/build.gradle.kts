plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    api(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoCommon)
    api(catalogLibs.findLibrary("yudao-spring-boot-starter-aop").get())
    compileOnly(catalogLibs.findLibrary("yudao-spring-web").get())
    compileOnly(catalogLibs.findLibrary("yudao-jakarta-servlet-api").get())

    compileOnly(catalogLibs.findLibrary("yudao-opentracing-util").get())
    compileOnly(catalogLibs.findLibrary("yudao-apm-toolkit-trace").get())
    compileOnly(catalogLibs.findLibrary("yudao-apm-toolkit-logback-1-x").get())
    compileOnly(catalogLibs.findLibrary("yudao-apm-toolkit-opentracing").get())
    compileOnly(catalogLibs.findLibrary("yudao-micrometer-registry-prometheus").get())
    compileOnly(catalogLibs.findLibrary("yudao-spring-boot-admin-starter-client").get())

    annotationProcessor(catalogLibs.findLibrary("yudao-spring-boot-configuration-processor").get())
}

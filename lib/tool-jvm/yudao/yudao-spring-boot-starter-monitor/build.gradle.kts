plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {

    api(projects.lib.toolJvm.yudao.yudaoCommon)
    api(catalogLibs.findLibrary("yudao-spring-boot-starter-aop").get())
    compileOnly(catalogLibs.findLibrary("yudao-spring-web").get())
    compileOnly(catalogLibs.findLibrary("yudao-javax-servlet-api").get())

    api(catalogLibs.findLibrary("yudao-opentracing-util").get())
    api(catalogLibs.findLibrary("yudao-apm-toolkit-trace").get())
    api(catalogLibs.findLibrary("yudao-apm-toolkit-logback-1-x").get())
    api(catalogLibs.findLibrary("yudao-apm-toolkit-opentracing").get())
    api(catalogLibs.findLibrary("yudao-micrometer-registry-prometheus").get())
    api(catalogLibs.findLibrary("yudao-spring-boot-admin-starter-client").get())

    annotationProcessor(catalogLibs.findLibrary("org-springframework-boot-spring-boot-configuration-processor-v2").get())
}

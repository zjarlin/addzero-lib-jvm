plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {

    api(projects.lib.toolJvm.yudao.yudaoCommon)
    compileOnly(projects.lib.toolJvm.yudao.yudaoSpringBootStarterSecurity)
    compileOnly(projects.lib.toolJvm.yudao.yudaoSpringBootStarterBizTenant)
    api(projects.lib.toolJvm.yudao.yudaoSpringBootStarterMq)
    api(catalogLibs.findLibrary("yudao-spring-boot-starter-websocket").get())

    compileOnly(catalogLibs.findLibrary("yudao-spring-kafka").get())
    compileOnly(catalogLibs.findLibrary("yudao-spring-rabbit").get())
    compileOnly(catalogLibs.findLibrary("yudao-rocketmq-spring-boot-starter").get())

    annotationProcessor(catalogLibs.findLibrary("org-springframework-boot-spring-boot-configuration-processor-v2").get())
}

plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    api(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoCommon)
    compileOnly(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterSecurity)
    compileOnly(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterBizTenant)
    api(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterMq)
    api(catalogLibs.findLibrary("yudao-spring-boot-starter-websocket").get())

    compileOnly(catalogLibs.findLibrary("yudao-spring-kafka").get())
    compileOnly(catalogLibs.findLibrary("yudao-spring-rabbit").get())
    compileOnly(catalogLibs.findLibrary("yudao-rocketmq-spring-boot-starter").get())

    annotationProcessor(catalogLibs.findLibrary("yudao-spring-boot-configuration-processor").get())
}

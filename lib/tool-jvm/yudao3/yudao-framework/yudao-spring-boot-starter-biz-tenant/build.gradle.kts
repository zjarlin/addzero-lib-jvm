plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    api(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoCommon)
    api(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterSecurity)
    api(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterMybatis)
    api(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterRedis)

    compileOnly(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterRpc)
    compileOnly(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterJob)
    compileOnly(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterMq)
    compileOnly(catalogLibs.findLibrary("yudao-spring-kafka").get())
    compileOnly(catalogLibs.findLibrary("yudao-spring-rabbit").get())
    compileOnly(catalogLibs.findLibrary("yudao-rocketmq-spring-boot-starter").get())

    implementation(catalogLibs.findLibrary("yudao-guava").get())

    annotationProcessor(catalogLibs.findLibrary("yudao-spring-boot-configuration-processor").get())
    testImplementation(catalogLibs.findLibrary("yudao-spring-boot-starter-test").get())
}

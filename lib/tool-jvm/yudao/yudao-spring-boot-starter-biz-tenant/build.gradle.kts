plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    api(projects.lib.toolJvm.yudao.yudaoCommon)
    api(projects.lib.toolJvm.yudao.yudaoSpringBootStarterSecurity)
    api(projects.lib.toolJvm.yudao.yudaoSpringBootStarterMybatis)
    api(projects.lib.toolJvm.yudao.yudaoSpringBootStarterRedis)

    compileOnly(projects.lib.toolJvm.yudao.yudaoSpringBootStarterRpc)
    compileOnly(projects.lib.toolJvm.yudao.yudaoSpringBootStarterJob)
    compileOnly(projects.lib.toolJvm.yudao.yudaoSpringBootStarterMq)
    compileOnly(catalogLibs.findLibrary("yudao-spring-kafka").get())
    compileOnly(catalogLibs.findLibrary("yudao-spring-rabbit").get())
    compileOnly(catalogLibs.findLibrary("yudao-rocketmq-spring-boot-starter").get())

    implementation(catalogLibs.findLibrary("yudao-guava").get())

    annotationProcessor(catalogLibs.findLibrary("yudao-spring-boot-configuration-processor").get())
    testImplementation(catalogLibs.findLibrary("yudao-spring-boot-starter-test").get())
}

plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {

    api(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterRedis)
    compileOnly(catalogLibs.findLibrary("yudao-spring-kafka").get())
    api(catalogLibs.findLibrary("yudao-spring-rabbit").get())
    api(catalogLibs.findLibrary("yudao-rocketmq-spring-boot-starter").get())
}

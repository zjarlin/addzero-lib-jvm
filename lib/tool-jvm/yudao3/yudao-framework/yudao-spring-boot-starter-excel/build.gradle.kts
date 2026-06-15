plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    api(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoCommon)
    api(catalogLibs.findLibrary("yudao-spring-boot-starter").get())
    compileOnly(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterRpc)
    compileOnly(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterBizIp)
    compileOnly(catalogLibs.findLibrary("yudao-spring-web").get())
    compileOnly(catalogLibs.findLibrary("yudao-jakarta-servlet-api").get())

    api(catalogLibs.findLibrary("yudao-easyexcel").get())
    api(catalogLibs.findLibrary("yudao-guava").get())
}

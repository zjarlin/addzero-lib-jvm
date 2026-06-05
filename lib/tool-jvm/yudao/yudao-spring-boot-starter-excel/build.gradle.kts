plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {

    api(projects.lib.toolJvm.yudao.yudaoCommon)
    api(catalogLibs.findLibrary("yudao-spring-boot-starter").get())
    compileOnly(projects.lib.toolJvm.yudao.yudaoSpringBootStarterBizIp)
    compileOnly(catalogLibs.findLibrary("yudao-spring-web").get())
    compileOnly(catalogLibs.findLibrary("yudao-javax-servlet-api").get())

    api(catalogLibs.findLibrary("yudao-easyexcel").get())
    api(catalogLibs.findLibrary("yudao-guava").get())
    api(catalogLibs.findLibrary("yudao-commons-compress").get())
}

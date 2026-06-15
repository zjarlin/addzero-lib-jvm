plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {

    api(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoCommon)
    api(catalogLibs.findLibrary("yudao-ip2region").get())
    compileOnly(catalogLibs.findLibrary("yudao-slf4j-api").get())
}

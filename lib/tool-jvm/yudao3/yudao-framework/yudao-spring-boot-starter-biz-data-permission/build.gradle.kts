plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    api(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoCommon)
    api(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterMybatis)
    compileOnly(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterSecurity)
    compileOnly(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterRpc)
}

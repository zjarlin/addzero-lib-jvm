plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    api(projects.lib.toolJvm.yudao.yudaoCommon)
    api(projects.lib.toolJvm.yudao.yudaoSpringBootStarterMybatis)
    compileOnly(projects.lib.toolJvm.yudao.yudaoSpringBootStarterSecurity)
    compileOnly(projects.lib.toolJvm.yudao.yudaoSpringBootStarterRpc)
}

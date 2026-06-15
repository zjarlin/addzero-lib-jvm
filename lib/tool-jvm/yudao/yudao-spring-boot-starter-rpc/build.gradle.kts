plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    api(projects.lib.toolJvm.yudao.yudaoCommon)

    api(catalogLibs.findLibrary("yudao-spring-cloud-starter-loadbalancer").get())
    api(catalogLibs.findLibrary("yudao-spring-cloud-starter-openfeign").get())
    api(catalogLibs.findLibrary("yudao-feign-okhttp").get())
    api(catalogLibs.findLibrary("yudao-jakarta-validation-api").get())
}

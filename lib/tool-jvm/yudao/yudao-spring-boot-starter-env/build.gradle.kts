plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    api(projects.lib.toolJvm.yudao.yudaoCommon)

    api(catalogLibs.findLibrary("yudao-spring-boot-starter").get())
    api(catalogLibs.findLibrary("yudao-spring-web").get())
    api(catalogLibs.findLibrary("yudao-jakarta-servlet-api").get())
    api(catalogLibs.findLibrary("yudao-spring-cloud-loadbalancer").get())
    api(catalogLibs.findLibrary("yudao-feign-core").get())
    api(catalogLibs.findLibrary("yudao-spring-cloud-starter-alibaba-nacos-discovery").get()) {
        exclude(group = "com.alibaba.nacos", module = "logback-adapter")
    }
}

plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    api(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoCommon)
    api(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterWeb)
    api(catalogLibs.findLibrary("yudao-spring-boot-starter-aop").get())
    api(catalogLibs.findLibrary("yudao-spring-boot-starter-security").get())
    compileOnly(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterRpc)

    implementation(catalogLibs.findLibrary("yudao-guava").get())
    api(catalogLibs.findLibrary("yudao-bizlog-sdk").get()) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter")
    }

    annotationProcessor(catalogLibs.findLibrary("yudao-spring-boot-configuration-processor").get())
}

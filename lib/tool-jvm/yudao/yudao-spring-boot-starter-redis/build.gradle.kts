plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    api(projects.lib.toolJvm.yudao.yudaoCommon)
    api(catalogLibs.findLibrary("yudao-redisson-spring-boot-starter").get())
    api(catalogLibs.findLibrary("yudao-spring-boot-starter-cache").get())
    api(catalogLibs.findLibrary("yudao-jackson-datatype-jsr310").get())

    annotationProcessor(catalogLibs.findLibrary("yudao-spring-boot-configuration-processor").get())
}

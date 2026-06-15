plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    api(projects.lib.toolJvm.yudao.yudaoCommon)
    compileOnly(catalogLibs.findLibrary("yudao-spring-boot-starter").get())

    api(catalogLibs.findLibrary("yudao-xxl-job-core").get())
    api(catalogLibs.findLibrary("yudao-jakarta-validation-api").get())

    annotationProcessor(catalogLibs.findLibrary("yudao-spring-boot-configuration-processor").get())
}

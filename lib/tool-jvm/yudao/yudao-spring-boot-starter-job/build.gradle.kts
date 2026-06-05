plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {

    api(projects.lib.toolJvm.yudao.yudaoCommon)
    compileOnly(catalogLibs.findLibrary("yudao-spring-boot-starter").get())

    api(catalogLibs.findLibrary("yudao-xxl-job-core").get())
    api(catalogLibs.findLibrary("yudao-javax-validation-api").get())

    annotationProcessor(catalogLibs.findLibrary("org-springframework-boot-spring-boot-configuration-processor-v2").get())
}

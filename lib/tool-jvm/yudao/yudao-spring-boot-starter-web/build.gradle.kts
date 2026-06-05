plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {

    api(projects.lib.toolJvm.yudao.yudaoCommon)
    api(catalogLibs.findLibrary("yudao-spring-boot-starter-web").get())
    api(catalogLibs.findLibrary("yudao-spring-boot-starter-validation").get())
    api(catalogLibs.findLibrary("yudao-knife4j-openapi3-spring-boot-starter").get())
    api(catalogLibs.findLibrary("yudao-springdoc-openapi-ui").get())

    compileOnly(catalogLibs.findLibrary("yudao-spring-security-core").get())
    compileOnly(catalogLibs.findLibrary("javax-annotation-javax-annotation-api").get())

    implementation(catalogLibs.findLibrary("yudao-jsoup").get())

    annotationProcessor(catalogLibs.findLibrary("org-springframework-boot-spring-boot-configuration-processor-v2").get())

    testImplementation(catalogLibs.findLibrary("org-springframework-boot-spring-boot-starter-test-v2").get())
    testImplementation(catalogLibs.findLibrary("yudao-mockito-inline").get())
}

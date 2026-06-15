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

    compileOnly(projects.lib.toolJvm.yudao.yudaoSpringBootStarterRpc)
    compileOnly(catalogLibs.findLibrary("yudao-spring-security-core").get())
    compileOnly(catalogLibs.findLibrary("yudao-guava").get())

    implementation(catalogLibs.findLibrary("yudao-jsoup").get())

    annotationProcessor(catalogLibs.findLibrary("yudao-spring-boot-configuration-processor").get())

    testImplementation(catalogLibs.findLibrary("yudao-spring-boot-starter-test").get())
    testImplementation(catalogLibs.findLibrary("yudao-mockito-inline").get())
}

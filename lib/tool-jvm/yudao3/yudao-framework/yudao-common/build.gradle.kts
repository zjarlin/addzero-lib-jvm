plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    compileOnly(catalogLibs.findLibrary("yudao-spring-core").get())
    compileOnly(catalogLibs.findLibrary("yudao-spring-expression").get())
    compileOnly(catalogLibs.findLibrary("yudao-spring-aop").get())
    compileOnly(catalogLibs.findLibrary("yudao-aspectjweaver").get())
    compileOnlyApi(catalogLibs.findLibrary("yudao-spring-web").get())
    compileOnlyApi(catalogLibs.findLibrary("yudao-jakarta-servlet-api").get())
    compileOnlyApi(catalogLibs.findLibrary("yudao-springdoc-openapi-ui").get())
    compileOnlyApi(catalogLibs.findLibrary("yudao-spring-cloud-openfeign-core").get())
    compileOnly(catalogLibs.findLibrary("yudao-guava").get())
    compileOnly(catalogLibs.findLibrary("yudao-jackson-databind").get())
    compileOnly(catalogLibs.findLibrary("yudao-jackson-core").get())
    compileOnly(catalogLibs.findLibrary("yudao-jackson-datatype-jsr310").get())
    compileOnly(catalogLibs.findLibrary("yudao-slf4j-api").get())
    compileOnlyApi(catalogLibs.findLibrary("yudao-jakarta-validation-api").get())

    api(catalogLibs.findLibrary("yudao-easy-trans-anno").get())
    implementation(catalogLibs.findLibrary("yudao-apm-toolkit-trace").get())
    api(catalogLibs.findLibrary("yudao-mapstruct").get())
    api(catalogLibs.findLibrary("yudao-mapstruct-jdk8").get())
    api(catalogLibs.findLibrary("yudao-hutool-all").get())
    api(catalogLibs.findLibrary("yudao-transmittable-thread-local").get())
    implementation(catalogLibs.findLibrary("yudao-jsoup").get())

    annotationProcessor(catalogLibs.findLibrary("yudao-mapstruct-processor").get())
    annotationProcessor(catalogLibs.findLibrary("yudao-spring-boot-configuration-processor").get())

    testImplementation(catalogLibs.findLibrary("yudao-spring-boot-starter-test").get())
}

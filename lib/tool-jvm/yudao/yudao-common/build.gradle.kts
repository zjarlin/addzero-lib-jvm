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
    compileOnlyApi(catalogLibs.findLibrary("yudao-javax-servlet-api").get())
    compileOnlyApi(catalogLibs.findLibrary("yudao-springdoc-openapi-ui").get())
    compileOnly(catalogLibs.findLibrary("yudao-guava").get())
    compileOnly(catalogLibs.findLibrary("yudao-jackson-databind").get())
    compileOnly(catalogLibs.findLibrary("yudao-jackson-core").get())
    compileOnly(catalogLibs.findLibrary("yudao-jackson-datatype-jsr310").get())
    compileOnly(catalogLibs.findLibrary("yudao-slf4j-api").get())
    compileOnlyApi(catalogLibs.findLibrary("yudao-javax-validation-api").get())

    api(catalogLibs.findLibrary("yudao-easy-trans-anno").get())

    implementation(catalogLibs.findLibrary("yudao-apm-toolkit-trace").get())
    implementation(catalogLibs.findLibrary("yudao-mapstruct").get())
    implementation(catalogLibs.findLibrary("yudao-mapstruct-jdk8").get())
    api(catalogLibs.findLibrary("yudao-hutool-all").get())
    api(catalogLibs.findLibrary("yudao-transmittable-thread-local").get())
    implementation(catalogLibs.findLibrary("yudao-jsoup").get())

    annotationProcessor(catalogLibs.findLibrary("yudao-mapstruct-processor").get())
    annotationProcessor(catalogLibs.findLibrary("org-springframework-boot-spring-boot-configuration-processor-v2").get())

    testImplementation(catalogLibs.findLibrary("org-springframework-boot-spring-boot-starter-test-v2").get())
}

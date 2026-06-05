plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {

    api(projects.lib.toolJvm.yudao.yudaoCommon)
    api(projects.lib.toolJvm.yudao.yudaoSpringBootStarterSecurity)
    api(projects.lib.toolJvm.yudao.yudaoSpringBootStarterMybatis)
    api(projects.lib.toolJvm.yudao.yudaoSpringBootStarterRedis)

    compileOnly(projects.lib.toolJvm.yudao.yudaoSpringBootStarterJob)
    compileOnly(projects.lib.toolJvm.yudao.yudaoSpringBootStarterMq)
    compileOnly(catalogLibs.findLibrary("yudao-spring-kafka").get())
    compileOnly(catalogLibs.findLibrary("yudao-spring-rabbit").get())
    compileOnly(catalogLibs.findLibrary("yudao-rocketmq-spring-boot-starter").get())
    compileOnly(catalogLibs.findLibrary("javax-annotation-javax-annotation-api").get())

    implementation(catalogLibs.findLibrary("yudao-guava").get())

    annotationProcessor(catalogLibs.findLibrary("org-springframework-boot-spring-boot-configuration-processor-v2").get())
    testImplementation(catalogLibs.findLibrary("org-springframework-boot-spring-boot-starter-test-v2").get())
}

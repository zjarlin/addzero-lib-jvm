plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {

    api(projects.lib.toolJvm.yudao.yudaoCommon)
    api(projects.lib.toolJvm.yudao.yudaoSpringBootStarterWeb)
    api(catalogLibs.findLibrary("yudao-spring-boot-starter-aop").get())
    api(catalogLibs.findLibrary("yudao-spring-boot-starter-security").get())

    compileOnly(catalogLibs.findLibrary("javax-annotation-javax-annotation-api").get())

    implementation(catalogLibs.findLibrary("yudao-guava").get())
    implementation(catalogLibs.findLibrary("yudao-bizlog-sdk").get()) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter")
    }

    annotationProcessor(catalogLibs.findLibrary("org-springframework-boot-spring-boot-configuration-processor-v2").get())
}

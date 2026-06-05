plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {

    api(projects.lib.toolJvm.yudao.yudaoCommon)
    api(catalogLibs.findLibrary("yudao-redisson-spring-boot-starter").get()) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-actuator")
        exclude(group = "org.redisson", module = "redisson-spring-data-34")
    }
    api(catalogLibs.findLibrary("yudao-redisson-spring-data-27").get())
    api(catalogLibs.findLibrary("yudao-spring-boot-starter-cache").get())
    api(catalogLibs.findLibrary("yudao-jackson-datatype-jsr310").get())

    annotationProcessor(catalogLibs.findLibrary("org-springframework-boot-spring-boot-configuration-processor-v2").get())
}

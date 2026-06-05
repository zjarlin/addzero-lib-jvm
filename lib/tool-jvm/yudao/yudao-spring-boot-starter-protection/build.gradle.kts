plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {

    compileOnly(projects.lib.toolJvm.yudao.yudaoSpringBootStarterWeb)
    api(projects.lib.toolJvm.yudao.yudaoSpringBootStarterRedis)

    compileOnly(catalogLibs.findLibrary("yudao-lock4j-redisson-spring-boot-starter").get()) {
        exclude(group = "org.redisson", module = "redisson-spring-boot-starter")
    }
}

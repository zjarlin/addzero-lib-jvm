plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {

    compileOnly(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterWeb)
    api(projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterRedis)

    compileOnly(catalogLibs.findLibrary("yudao-lock4j-redisson-spring-boot-starter").get()) {
        exclude(group = "org.redisson", module = "redisson-spring-boot-starter")
    }
}

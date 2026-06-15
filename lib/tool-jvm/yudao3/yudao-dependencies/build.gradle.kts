plugins {
    `java-platform`
    id("site.addzero.gradle.plugin.publish-buddy")
}

val catalogLibs = versionCatalogs.named("libs")
val yudaoProjects = listOf(
    projects.lib.toolJvm.yudao3.yudaoFramework.yudaoCommon,
    projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterBizDataPermission,
    projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterBizIp,
    projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterBizTenant,
    projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterEnv,
    projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterExcel,
    projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterJob,
    projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterMonitor,
    projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterMq,
    projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterMybatis,
    projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterProtection,
    projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterRedis,
    projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterRpc,
    projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterSecurity,
    projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterWeb,
    projects.lib.toolJvm.yudao3.yudaoFramework.yudaoSpringBootStarterWebsocket,
)
val managedLibraries = listOf(
    "yudao-spring-boot-configuration-processor",
    "yudao-spring-boot-starter-test",
    "yudao-spring-cloud-starter-alibaba-nacos-discovery",
    "yudao-springdoc-openapi-ui",
    "yudao-knife4j-openapi3-spring-boot-starter",
    "yudao-mybatis",
    "yudao-mysql-connector-j",
    "yudao-druid-spring-boot-starter",
    "yudao-mybatis-plus-boot-starter",
    "yudao-mybatis-plus-jsqlparser",
    "yudao-mybatis-plus-generator",
    "yudao-dynamic-datasource-spring-boot-starter",
    "yudao-mybatis-plus-join-boot-starter",
    "yudao-easy-trans-anno",
    "yudao-easy-trans-spring-boot-starter",
    "yudao-easy-trans-mybatis-plus-extend",
    "yudao-redisson-spring-boot-starter",
    "yudao-rocketmq-spring-boot-starter",
    "yudao-xxl-job-core",
    "yudao-lock4j-redisson-spring-boot-starter",
    "yudao-apm-toolkit-trace",
    "yudao-apm-toolkit-logback-1-x",
    "yudao-apm-toolkit-opentracing",
    "yudao-opentracing-util",
    "yudao-spring-boot-admin-starter-client",
    "yudao-ip2region",
    "yudao-easyexcel",
    "yudao-commons-compress",
    "yudao-commons-io",
    "yudao-commons-net",
    "yudao-transmittable-thread-local",
    "yudao-bizlog-sdk",
    "yudao-hutool-all",
    "yudao-jsoup",
    "yudao-mapstruct",
    "yudao-mapstruct-jdk8",
    "yudao-mapstruct-processor",
    "yudao-guava",
    "yudao-mockito-inline",
)

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform(catalogLibs.findLibrary("yudao-spring-boot-dependencies").get()))
    api(platform(catalogLibs.findLibrary("yudao-spring-cloud-dependencies").get()))
    api(platform(catalogLibs.findLibrary("yudao-spring-cloud-alibaba-dependencies").get()))

    constraints {
        yudaoProjects.forEach { api(it) }
        managedLibraries.forEach { alias ->
            api(catalogLibs.findLibrary(alias).get())
        }
    }
}

plugins {
    `java-platform`
    id("site.addzero.gradle.plugin.publish-buddy")
}

val catalogLibs = versionCatalogs.named("libs")
val yudaoProjects = listOf(
    projects.lib.toolJvm.yudao.yudaoCommon,
    projects.lib.toolJvm.yudao.yudaoSpringBootStarterBizDataPermission,
    projects.lib.toolJvm.yudao.yudaoSpringBootStarterBizIp,
    projects.lib.toolJvm.yudao.yudaoSpringBootStarterBizTenant,
    projects.lib.toolJvm.yudao.yudaoSpringBootStarterCaptcha,
    projects.lib.toolJvm.yudao.yudaoSpringBootStarterEnv,
    projects.lib.toolJvm.yudao.yudaoSpringBootStarterExcel,
    projects.lib.toolJvm.yudao.yudaoSpringBootStarterJob,
    projects.lib.toolJvm.yudao.yudaoSpringBootStarterMonitor,
    projects.lib.toolJvm.yudao.yudaoSpringBootStarterMq,
    projects.lib.toolJvm.yudao.yudaoSpringBootStarterMybatis,
    projects.lib.toolJvm.yudao.yudaoSpringBootStarterProtection,
    projects.lib.toolJvm.yudao.yudaoSpringBootStarterRedis,
    projects.lib.toolJvm.yudao.yudaoSpringBootStarterRpc,
    projects.lib.toolJvm.yudao.yudaoSpringBootStarterSecurity,
    projects.lib.toolJvm.yudao.yudaoSpringBootStarterWeb,
    projects.lib.toolJvm.yudao.yudaoSpringBootStarterWebsocket,
)
val managedLibraries = listOf(
    "yudao-spring-boot-dependencies",
    "yudao-spring-cloud-dependencies",
    "yudao-spring-cloud-alibaba-dependencies",
    "yudao-spring-boot-configuration-processor",
    "yudao-spring-boot-starter",
    "yudao-spring-boot-starter-aop",
    "yudao-spring-boot-starter-cache",
    "yudao-spring-boot-starter-security",
    "yudao-spring-boot-starter-test",
    "yudao-spring-boot-starter-validation",
    "yudao-spring-boot-starter-web",
    "yudao-spring-boot-starter-websocket",
    "yudao-spring-cloud-loadbalancer",
    "yudao-spring-cloud-openfeign-core",
    "yudao-spring-cloud-starter-loadbalancer",
    "yudao-spring-cloud-starter-openfeign",
    "yudao-spring-cloud-starter-alibaba-nacos-discovery",
    "yudao-feign-core",
    "yudao-feign-okhttp",
    "yudao-jakarta-servlet-api",
    "yudao-jakarta-validation-api",
    "yudao-aspectjweaver",
    "yudao-slf4j-api",
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
    "yudao-micrometer-registry-prometheus",
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

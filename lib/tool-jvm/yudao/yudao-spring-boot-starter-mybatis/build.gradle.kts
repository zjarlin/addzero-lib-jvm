plugins {
    id("site.addzero.buildlogic.yudao.yudao-java-starter")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    api(projects.lib.toolJvm.yudao.yudaoCommon)
    compileOnly(projects.lib.toolJvm.yudao.yudaoSpringBootStarterSecurity)

    api(catalogLibs.findLibrary("yudao-mysql-connector-j").get())
    compileOnly(catalogLibs.findLibrary("yudao-ojdbc8").get())
    compileOnly(catalogLibs.findLibrary("yudao-postgresql").get())
    compileOnly(catalogLibs.findLibrary("yudao-mssql-jdbc").get())
    compileOnly(catalogLibs.findLibrary("yudao-dm-jdbc-driver18").get())
    compileOnly(catalogLibs.findLibrary("yudao-kingbase8").get())
    compileOnly(catalogLibs.findLibrary("yudao-opengauss-jdbc").get())

    api(catalogLibs.findLibrary("yudao-druid-spring-boot-starter").get())
    api(catalogLibs.findLibrary("yudao-mybatis").get())
    api(catalogLibs.findLibrary("yudao-mybatis-plus-boot-starter").get())
    api(catalogLibs.findLibrary("yudao-mybatis-plus-jsqlparser").get())
    api(catalogLibs.findLibrary("yudao-dynamic-datasource-spring-boot-starter").get()) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-undertow")
    }
    api(catalogLibs.findLibrary("yudao-mybatis-plus-join-boot-starter").get())
    api(catalogLibs.findLibrary("yudao-easy-trans-spring-boot-starter").get()) {
        exclude(group = "org.springframework", module = "spring-context")
        exclude(group = "org.springframework.cloud", module = "spring-cloud-commons")
    }
    api(catalogLibs.findLibrary("yudao-easy-trans-mybatis-plus-extend").get())

    annotationProcessor(catalogLibs.findLibrary("yudao-spring-boot-configuration-processor").get())
}

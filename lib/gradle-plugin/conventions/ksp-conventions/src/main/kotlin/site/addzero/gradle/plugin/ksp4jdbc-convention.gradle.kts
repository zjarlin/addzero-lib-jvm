package site.addzero.gradle.plugin

import site.addzero.util.DatabaseConfigReader
import site.addzero.util.YmlUtil
import site.addzero.util.YmlUtil.replaceEnvInString

plugins {
    id("com.google.devtools.ksp")
}

val serverProject = project(":backend:server")
val serverResourceDir = serverProject.projectDir.resolve("src/main/resources").absolutePath

val fromSpringYml = DatabaseConfigReader.fromSpringYml(serverResourceDir)
//fromSpringYml.sc

val ymlPath = "$serverResourceDir/application.yml"

val activate = YmlUtil.getActivateBydir(ymlPath)
val ymlActivetePath = "$serverResourceDir/application-${'$'}activate.yml"
val ymlActiveConfig = YmlUtil.loadYmlConfigMap(ymlActivetePath)

val activeDatasource = YmlUtil.getConfigValue<String>(ymlActiveConfig, "spring.datasource.dynamic.primary")

val pgdatasource = YmlUtil.getConfigValue<Map<String, Any>>(ymlActiveConfig, "spring.datasource.dynamic.datasource")

val map = if (pgdatasource == null) {
    YmlUtil.getConfigValue<Map<String, Any>>(ymlActiveConfig, "spring.datasource")
} else {
    pgdatasource[activeDatasource]
}
val datasource = map as Map<String, String>

val jdbcDriver = datasource["driver-class-name"]!!
val url = datasource["url"].replaceEnvInString()
val urlSplit = url.split("?")

val jdbcUrl = urlSplit.first()
val jdbcSchema = urlSplit.last().split("=").last()

val jdbcUsername = datasource["username"].replaceEnvInString()
val jdbcPassword = datasource["password"].replaceEnvInString()
val excludeTables = datasource["exclude-tables"].replaceEnvInString()

ksp {
    arg("jdbcUrl", jdbcUrl)
    arg("jdbcUsername", jdbcUsername)
    arg("jdbcPassword", jdbcPassword)
    arg("jdbcSchema", jdbcSchema)
    arg("jdbcDriver", jdbcDriver)
    arg("excludeTables", excludeTables)
    arg("baseEntityPackage", "site.addzero.model.common.BaseEntity")

    println("jdbcUrl: ${'$'}jdbcUrl")
    println("jdbcUsername: ${'$'}jdbcUsername")
    println("jdbcPassword: ${'$'}jdbcPassword")
    println("jdbcSchema: ${'$'}jdbcSchema")
    println("jdbcDriver: ${'$'}jdbcDriver")
    println("excludeTables: ${'$'}excludeTables")
}

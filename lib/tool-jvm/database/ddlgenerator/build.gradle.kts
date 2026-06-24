plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
  id("site.addzero.gradle.plugin.processor-buddy") version "2026.01.11"
}
val libs = versionCatalogs.named("libs")

val jdbcConfigMap = mapOf(
    //以下三个可选,会从application.yml或者激活的配置文件中猜测
    "jdbcUrl" to "",
    "jdbcUsername" to "",
    "jdbcPassword" to "",
)
val autoddlSwitch = mapOf(
    //ddl是否包含外键
    "autoddlForeignKeys" to "true",
    //ddl是否包含唯一索引
    "autoddlKeys" to "true",
    //ddl是否删除列
    "autoddlAllowDeleteColumn" to "false"
)
val autoddlConfigMap = jdbcConfigMap + autoddlSwitch + mapOf(
    "springResourcePath" to "",
    "autoddlExcludeTables" to "flyway_schema_history,vector_store,*_mapping",
    "autoddlExcludeColumns" to "",
)

processorBuddy {
  mustMap.set(autoddlConfigMap)
  packageName.set("site.addzero.ddlgenerator.runtime.config")
  readmeEnabled.set(false)
}
dependencies {
  api(project(":lib:tool-jvm:database:ddlgenerator-core"))
  api(project(":lib:tool-jvm:database:ddlgenerator-lsi-adaptor"))
  api(project(":lib:tool-jvm:database:ddlgenerator-jdbc-adaptor"))
  implementation(project(":lib:tool-jvm:database:ddlgenerator-dialect-mysql"))
  implementation(project(":lib:tool-jvm:database:ddlgenerator-dialect-postgresql"))
  implementation(project(":lib:tool-jvm:database:ddlgenerator-dialect-h2"))
  implementation(project(":lib:tool-jvm:database:ddlgenerator-dialect-sqlite"))
  implementation(project(":lib:tool-jvm:database:ddlgenerator-dialect-sqlserver"))
  implementation(project(":lib:tool-jvm:database:ddlgenerator-dialect-oracle"))
  implementation(project(":lib:tool-jvm:database:ddlgenerator-dialect-dm"))
  implementation(project(":lib:tool-jvm:database:ddlgenerator-dialect-kingbase"))
  implementation(project(":lib:tool-jvm:database:ddlgenerator-dialect-taos"))
  implementation(project(":lib:tool-kmp:jdbc:tool-jdbc"))
  implementation(project(":lib:tool-kmp:jdbc:tool-jdbc-model"))
  implementation(project(":lib:tool-jvm:database:tool-database-model"))
  implementation(libs.findLibrary("site-addzero-tool-yml").get())
  testImplementation(libs.findLibrary("com-h2database-h2").get())
}
description = "AutoDDL 运行时集成层"

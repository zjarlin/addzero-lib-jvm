plugins {
  id("site.addzero.gradle.plugin.kotlin-convention") version "+"
  id("site.addzero.gradle.plugin.koin-convention") version "+"
  id("site.addzero.gradle.plugin.processorbuddy.processor-buddy") version "2026.01.11"
}

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
    //spring资源目录,用于猜测配置
    "springResourcePath" to "",
    //删除表的时候排除哪些表
    "autoddlExcludeTables" to "flyway_schema_history,vector_store,*_mapping",
    //删除列的时候排除哪些列
//    "autoddlExcludeColumns" to "",
)

processorBuddy {
  mustMap.set(autoddlConfigMap)
  packageName.set("org.babyfish.jimmer.config.autoddl")
  readmeEnabled.set(true)
}
dependencies {
  implementation(libs.jimmer.core)
  implementation(libs.tool.coll)
//  implementation(libs.lsi.core)
  implementation(libs.tool.database.model)
  implementation(libs.tool.jdbc)
  implementation(libs.tool.yml)
  implementation(libs.tool.str)
  implementation(libs.tool.sql.executor)
}
description = "ddl生成工具类"

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    api(project(":lib:tool-jvm:database:ddlgenerator-core"))
    api(project(":lib:tool-kmp:jdbc:tool-jdbc-model"))
}

description = "AutoDDL 的 JDBC 元数据适配层"

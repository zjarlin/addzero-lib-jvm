plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
    api(project(":lib:tool-jvm:database:ddlgenerator-core"))
    api(libs.findLibrary("site-addzero-tool-jdbc-model").get())
}

description = "AutoDDL 的 JDBC 元数据适配层"

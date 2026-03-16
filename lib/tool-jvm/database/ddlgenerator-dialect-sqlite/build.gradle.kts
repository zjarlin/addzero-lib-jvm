plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    api(project(":lib:tool-jvm:database:ddlgenerator-core"))
}

description = "AutoDDL SQLite 方言"

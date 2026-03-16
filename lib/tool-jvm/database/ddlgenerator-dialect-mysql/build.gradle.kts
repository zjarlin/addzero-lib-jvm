plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    api(project(":lib:tool-jvm:database:ddlgenerator-core"))
}

description = "AutoDDL MySQL 方言"

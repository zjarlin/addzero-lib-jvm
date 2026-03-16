plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    api(project(":lib:tool-jvm:database:tool-database-model"))
}

description = "AutoDDL 纯领域层：Schema 模型、Diff Planner、方言 SPI"

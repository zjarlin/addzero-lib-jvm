plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
    api(libs.findLibrary("site-addzero-tool-database-model").get())
}

description = "AutoDDL 纯领域层：Schema 模型、Diff Planner、方言 SPI"

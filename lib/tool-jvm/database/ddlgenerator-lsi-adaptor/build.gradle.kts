plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
    api(project(":lib:tool-jvm:database:ddlgenerator-core"))
    api(project(":lib:lsi:lsi-core"))
    implementation(libs.findLibrary("org-babyfish-jimmer-jimmer-core").get())
}

description = "AutoDDL 的 LSI 输入适配层"

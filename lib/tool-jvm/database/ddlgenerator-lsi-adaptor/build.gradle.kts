plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    api(project(":lib:tool-jvm:database:ddlgenerator-core"))
    api(libs.site.addzero.lsi.core)
    implementation(libs.org.babyfish.jimmer.jimmer.core)
}

description = "AutoDDL 的 LSI 输入适配层"

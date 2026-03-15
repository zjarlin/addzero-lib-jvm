package site.addzero.buildlogic.jvm

plugins {
    id("site.addzero.buildlogic.jvm.jvm-json")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("site-addzero-tool-json").get())
}

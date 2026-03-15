package site.addzero.buildlogic.jvm

plugins {
    id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
}

val libs = versionCatalogs.named("libs")

dependencies{
    ksp(libs.findLibrary("org-babyfish-jimmer-jimmer-ksp").get())
    implementation(libs.findLibrary("org-babyfish-jimmer-jimmer-sql-kotlin").get())
}

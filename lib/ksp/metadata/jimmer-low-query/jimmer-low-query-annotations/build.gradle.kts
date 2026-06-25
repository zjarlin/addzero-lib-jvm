plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

version = "2026.06.26"

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    compileOnly(catalogLibs.findLibrary("org-babyfish-jimmer-jimmer-sql-kotlin").get())
}

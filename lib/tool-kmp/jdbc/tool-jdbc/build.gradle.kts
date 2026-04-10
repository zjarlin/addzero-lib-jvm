plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies {
    api(libs.findLibrary("site-addzero-tool-jdbc-model").get())
    implementation(libs.findLibrary("site-addzero-tool-sql-executor").get())
}

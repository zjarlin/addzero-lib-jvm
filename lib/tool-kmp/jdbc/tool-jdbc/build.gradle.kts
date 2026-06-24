plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies {
    api(project(":lib:tool-kmp:jdbc:tool-jdbc-model"))
    implementation(project(":lib:tool-jvm:database:tool-sql-executor"))
}

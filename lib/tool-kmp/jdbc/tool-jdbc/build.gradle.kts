plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
dependencies {
    api(project(":lib:tool-kmp:jdbc:tool-jdbc-model"))
    implementation(libs.site.addzero.tool.sql.executor)
}

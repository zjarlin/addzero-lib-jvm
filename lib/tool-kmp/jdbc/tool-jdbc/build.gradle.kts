plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
dependencies {
    api(libs.tool.jdbc.model)
    implementation(libs.tool.sql.executor)
}

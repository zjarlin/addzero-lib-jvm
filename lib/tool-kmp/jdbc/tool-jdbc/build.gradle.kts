plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
dependencies {
    api(libs.site.addzero.tool.jdbc.model)
    implementation(libs.site.addzero.tool.sql.executor)
}

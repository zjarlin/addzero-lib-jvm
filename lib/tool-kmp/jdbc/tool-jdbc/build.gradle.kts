plugins {
    id("site.addzero.gradle.plugin.kotlin-convention")
}
dependencies {
    api(libs.tool.jdbc.model)
    implementation(libs.tool.sql.executor)
}

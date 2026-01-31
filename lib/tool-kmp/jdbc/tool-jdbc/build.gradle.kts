plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}
dependencies {
    api(libs.tool.jdbc.model)
    implementation(libs.tool.sql.executor)
}

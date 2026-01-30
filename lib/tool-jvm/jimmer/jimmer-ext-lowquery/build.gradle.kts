plugins {
  id("site.addzero.gradle.plugin.kotlin-convention")
}

dependencies {
    implementation(libs.jimmer.sql.kotlin)
    implementation(libs.hutool.all)
    api(libs.addzero.jimmer.model.lowquery)

}

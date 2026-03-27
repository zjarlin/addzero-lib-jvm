plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("org-babyfish-jimmer-jimmer-sql-kotlin").get())
    implementation(libs.findLibrary("cn-hutool-hutool-all").get())
    api(libs.findLibrary("site-addzero-jimmer-model-lowquery-v2025").get())

}

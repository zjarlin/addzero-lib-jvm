plugins {
  id("site.addzero.buildlogic.jvm.jvm-json")
  id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
  id("site.addzero.gradle.plugin.koin-convention")  version "2026.01.17"
}

dependencies {
  implementation(libs.okhttp)
  // Common models
  implementation("site.addzero:common-models:2026.01.20")


}

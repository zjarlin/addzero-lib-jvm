plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
  id("site.addzero.buildlogic.jvm.jvm-json-withtool")
}
val libs = versionCatalogs.named("libs")

dependencies {
  implementation(libs.findLibrary("com-squareup-okhttp3-okhttp").get())

  // Common models
  implementation(libs.findLibrary("common-models").get())

  // Test Implementation
  testImplementation(libs.findLibrary("com-squareup-okhttp3-mockwebserver").get())
}

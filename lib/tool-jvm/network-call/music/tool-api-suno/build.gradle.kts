plugins {
  id("site.addzero.buildlogic.jvm.jvm-json-withtool")
}
val libs = versionCatalogs.named("libs")

dependencies {
  implementation(libs.findLibrary("com-squareup-okhttp3-okhttp").get())

  // Common models
  implementation("site.addzero:common-models:2026.02.06")

  // Test Implementation
  testImplementation("com.squareup.okhttp3:mockwebserver:5.3.2")
}


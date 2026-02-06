plugins {
  id("site.addzero.buildlogic.jvm.jvm-json-withtool")
}

dependencies {
  implementation(libs.okhttp)

  // Common models
 implementation("site.addzero:common-models:2026.01.20")

  // Test Implementation
  testImplementation("com.squareup.okhttp3:mockwebserver:5.3.2")
}
version = "2026.02.06"


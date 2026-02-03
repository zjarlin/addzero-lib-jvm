plugins {
  id("site.addzero.buildlogic.jvm.jvm-json-withtool")
}

dependencies {
  implementation(libs.okhttp)

  // Common models
  implementation(project(":lib:tool-kmp:models:common:common-models"))

  // Test Implementation
  testImplementation("com.squareup.okhttp3:mockwebserver:5.3.2")
}


plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
  implementation(libs.com.squareup.okhttp3.okhttp)
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
}

plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
  implementation(libs.findLibrary("com-squareup-okhttp3-okhttp").get())
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
}

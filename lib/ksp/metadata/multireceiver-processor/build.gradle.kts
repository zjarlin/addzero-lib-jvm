plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
  implementation(libs.findLibrary("kcp-multireceiver-annotations").get())
  implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
}

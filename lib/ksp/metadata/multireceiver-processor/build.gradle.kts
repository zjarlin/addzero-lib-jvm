plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
  implementation(project(":lib:kcp:multireceiver:kcp-multireceiver-annotations"))
  implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
}

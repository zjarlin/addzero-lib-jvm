plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val catalogLibs = versionCatalogs.named("libs")

dependencies {
  implementation(libs.net.bytebuddy.byte.buddy)
  implementation(catalogLibs.findLibrary("site-addzero-tool-reflection").get())
}

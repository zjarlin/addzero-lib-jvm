plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
  implementation(libs.findLibrary("tool-api-music-search").get())
  implementation(libs.findLibrary("tool-api-suno").get())
}


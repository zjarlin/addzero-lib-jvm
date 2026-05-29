plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
  implementation(project(":lib:tool-jvm:network-call:music:tool-api-music-search"))
  implementation(project(":lib:tool-jvm:network-call:music:tool-api-suno"))
}

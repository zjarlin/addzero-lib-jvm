plugins {
  id("site.addzero.gradle.plugin.kotlin-convention")
}

dependencies {
  implementation(libs.byte.buddy)
  implementation(libs.tool.reflection)
}

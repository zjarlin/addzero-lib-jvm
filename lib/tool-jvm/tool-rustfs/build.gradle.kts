plugins {
  id("site.addzero.gradle.plugin.kotlin-convention")
}

dependencies {
  implementation(libs.s3)
  implementation(libs.slf4j.api)
  implementation(libs.caffeine)
  implementation("site.addzero:addzero-tool-common-jvm:2025.09.29")
}

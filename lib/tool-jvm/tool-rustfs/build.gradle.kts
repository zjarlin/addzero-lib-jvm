plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
  implementation(libs.software.amazon.awssdk.s3)
  implementation(libs.org.slf4j.slf4j.api)
  implementation(libs.com.github.ben.manes.caffeine.caffeine)
  implementation("site.addzero:addzero-tool-common-jvm:2025.09.29")
}

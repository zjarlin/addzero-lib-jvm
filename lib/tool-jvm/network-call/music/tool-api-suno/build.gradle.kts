plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
  kotlin("plugin.serialization") version "2.1.0"
}

dependencies {
  implementation(libs.okhttp)

  // Kotlinx Serialization
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")


  // Common models
  implementation(project(":lib:tool-kmp:models:common:common-models"))

}


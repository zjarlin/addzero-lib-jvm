package site.addzero.buildlogic.kmp

plugins {
  kotlin("plugin.serialization")
  id("site.addzero.buildlogic.kmp.kmp-convention")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(versionCatalogs.named("libs").findLibrary("org-jetbrains-kotlinx-kotlinx-serialization-json").get())
    }
  }
}

package site.addzero.buildlogic.kmp

plugins {
  id("site.addzero.buildlogic.kmp.kmp-json")
}

kotlin {
  dependencies {
    implementation(versionCatalogs.named("libs").findLibrary("site-addzero-tool-json").get())
  }
}

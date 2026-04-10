plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib")
  id("site.addzero.buildlogic.kmp.kmp-koin")
  id("site.addzero.buildlogic.kmp.kmp-json")
}
val libs = versionCatalogs.named("libs")

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.findLibrary("compose-workbench-design").get())
    }
  }
}

version="2026.04.11"

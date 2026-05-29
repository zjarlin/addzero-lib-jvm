plugins {
  id("site.addzero.buildlogic.kmp.kmp-core")
  id("site.addzero.buildlogic.kmp.kmp-koin-core")
  id("site.addzero.buildlogic.kmp.kmp-json")
}

val libs = versionCatalogs.named("libs")

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.findLibrary("ai-koog-koog-agents").get())
    }
  }
}

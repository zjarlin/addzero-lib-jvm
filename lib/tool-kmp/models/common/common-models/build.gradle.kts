plugins {
  id("site.addzero.buildlogic.kmp.kmp-json-withtool")
  id("site.addzero.buildlogic.kmp.kmp-core")
}
val libs = versionCatalogs.named("libs")

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.findLibrary("site-addzero-tool-str").get())
    }

  }
}



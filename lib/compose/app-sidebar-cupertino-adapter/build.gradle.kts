plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib")
}
val libs = versionCatalogs.named("libs")

kotlin {
  dependencies{
    api(libs.findLibrary("app-sidebar").get())
  }
}

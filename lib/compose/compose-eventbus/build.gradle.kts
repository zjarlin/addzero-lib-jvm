plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib")
}
val libs = versionCatalogs.named("libs")

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(project(":lib:compose:compose-native-component-toast"))
      implementation(libs.findLibrary("io-ktor-ktor-client-core").get())
      implementation(libs.findLibrary("site-addzero-network-starter").get())
      implementation(libs.findLibrary("site-addzero-tool-koin-v2025").get())
    }
  }
}

plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib")
}

val libs = versionCatalogs.named("libs")

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-core").get())
      api(libs.findLibrary("site-addzero-compose-native-component-table-core").get())
      api(project(":lib:tool-kmp:tool-model"))
    }

  }
}

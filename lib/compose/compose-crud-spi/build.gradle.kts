plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib")
}

val libs = versionCatalogs.named("libs")

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-core").get())
      api(project(":lib:compose:compose-native-component-table-core"))
      api(project(":lib:tool-kmp:tool-model"))
    }

    commonTest.dependencies {
      implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-test").get())
      implementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
    }
  }
}

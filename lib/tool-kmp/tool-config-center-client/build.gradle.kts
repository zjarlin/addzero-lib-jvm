plugins {
  id("site.addzero.buildlogic.kmp.kmp-ktor-client")
  id("site.addzero.buildlogic.kmp.cmp-android-lib")
  id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}

val libs = versionCatalogs.named("libs")

version = "2026.06.13"

kotlin {
  sourceSets {
    androidMain.dependencies {
      implementation(libs.findLibrary("io-ktor-ktor-client-okhttp").get())
    }

    commonTest.dependencies {
      implementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
      implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-test").get())
    }
  }
}

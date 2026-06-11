plugins {
  id("site.addzero.buildlogic.kmp.kmp-ktor-client")
  id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}

val libs = versionCatalogs.named("libs")

kotlin {
  sourceSets {
    commonTest.dependencies {
      implementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
      implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-test").get())
    }
  }
}

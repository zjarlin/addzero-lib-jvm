plugins {
  id("site.addzero.buildlogic.kmp.kmp-ksp")
  id("site.addzero.gradle.plugin.processor-buddy") version "+"
}
val libs = versionCatalogs.named("libs")

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.findLibrary("site-addzero-route-core").get())
//            implementation(libs.findLibrary("site-addzero-ksp-support").get())
    }
  }
}

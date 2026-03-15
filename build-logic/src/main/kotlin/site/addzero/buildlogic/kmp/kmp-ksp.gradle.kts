package site.addzero.buildlogic.kmp

plugins {
  id("site.addzero.buildlogic.kmp.kmp-convention")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(versionCatalogs.named("libs").findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
    }
  }
}



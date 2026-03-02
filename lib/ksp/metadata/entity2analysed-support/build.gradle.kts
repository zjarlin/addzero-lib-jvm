plugins {
  id("site.addzero.buildlogic.kmp.kmp-ksp")
}

kotlin {
  sourceSets {
    commonMain.dependencies {}
    jvmMain.dependencies {
      implementation("site.addzero:lsi-ksp:2026.02.26")
    }

  }
}

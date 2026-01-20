plugins {
  id("kmp-ksp")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation("site.addzero:singleton-adapter-api:2026.01.20")
    }
    jvmMain.dependencies {
      implementation(libs.kotlinpoet.ksp)
    }
  }
}

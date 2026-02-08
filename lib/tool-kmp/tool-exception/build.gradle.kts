plugins {
  id("site.addzero.buildlogic.kmp.kmp-core")
}
kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation("site.addzero:tool-enum:2026.02.06")
    }
  }
}

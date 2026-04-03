plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(project(":lib:compose:compose-sheet-spi"))
    }
  }
}

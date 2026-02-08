plugins {
  id("site.addzero.buildlogic.kmp.composition.kmp-json-withtool")
  id("site.addzero.buildlogic.kmp.platform.kmp-core")
}
kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.tool.str)
    }

  }
}



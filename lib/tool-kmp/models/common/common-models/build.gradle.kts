plugins {
  id("site.addzero.buildlogic.kmp.kmp-json-withtool")
  id("site.addzero.buildlogic.kmp.kmp-core")
}
kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.site.addzero.tool.str)
    }

  }
}



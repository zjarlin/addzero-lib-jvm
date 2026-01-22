plugins {
  id("site.addzero.buildlogic.kmp.composition.kmp-component")
  id("site.addzero.buildlogic.kmp.composition.kmp-json-withtool")
}


kotlin {
  sourceSets {
    commonMain.dependencies {
//      implementation(projects.lib.toolKmp.tool)

    }
  }
}

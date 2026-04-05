plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(project(":lib:compose:compose-native-component-toast"))
      implementation(project(":lib:tool-kmp:network-starter"))
      implementation(project(":lib:tool-kmp:tool-koin"))
    }
  }
}

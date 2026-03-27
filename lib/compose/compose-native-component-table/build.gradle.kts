plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib")
  id("site.addzero.buildlogic.kmp.kmp-json-withtool")
//    id("kmp-koin")
//    id("kmp-ksp-plugin")
}
//dependencies {
//    kspCommonMainMetadata(project(":lib:compose:compose-props-processor"))
//}
kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(project(":lib:compose:compose-native-component-card"))
//            implementation(project(":lib:compose:compose-native-component"))
//            implementation(project(":lib:tool-kmp:tool-json"))
      implementation("site.addzero:compose-props-annotations:2025.09.30")
      api(project(":lib:compose:compose-native-component-table-core"))
    }
  }
}

plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib-cupertino")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(project(":lib:compose:scaffold-spi"))
      implementation(project(":lib:compose:app-sidebar"))
      implementation(project(":lib:compose:app-sidebar-cupertino-adapter"))
      implementation(project(":lib:compose:compose-native-component-searchbar"))
      implementation(project(":lib:compose:compose-native-component-tree"))
      implementation(compose.material3)
    }
  }
}

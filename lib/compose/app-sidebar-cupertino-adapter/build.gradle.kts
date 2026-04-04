plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib-cupertino")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(project(":lib:compose:app-sidebar"))
      implementation(compose.material3)
    }
  }
}

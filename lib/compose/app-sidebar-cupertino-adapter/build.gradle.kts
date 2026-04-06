plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
  dependencies{
    api(project(":lib:compose:app-sidebar"))
  }
}

plugins {
  id("site.addzero.buildlogic.kmp.platform.kmp-core") version "+"
}
kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.addzero.tool.str)

//      implementation(project(":addzero-lib-jvm:lib:tool-kmp:tool11"))
    }

  }
}

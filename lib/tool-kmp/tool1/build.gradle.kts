plugins {
  id("site.addzero.gradle.plugin.kmp-core-convention") version "+"
}
kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.addzero.tool.str)

//      implementation(project(":addzero-lib-jvm:lib:tool-kmp:tool11"))
    }

  }
}

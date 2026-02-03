plugins {
  id("site.addzero.buildlogic.kmp.composition.kmp-json-withtool")
  id("site.addzero.buildlogic.kmp.platform.kmp-core")
}
// 配置 Kotlin 编译器选项以启用新特性
kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.tool.str)
    }

  }
}



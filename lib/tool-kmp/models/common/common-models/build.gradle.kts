plugins {
  id("site.addzero.buildlogic.kmp.libs.kmp-json")
  id("site.addzero.gradle.plugin.kmp-core-convention") version "+"
}
// 配置 Kotlin 编译器选项以启用新特性
kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.tool.str)
    }

  }
}



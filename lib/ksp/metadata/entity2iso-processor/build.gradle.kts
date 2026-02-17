plugins {
  id("site.addzero.buildlogic.kmp.kmp-ksp")
  alias(libs.plugins.site.addzero.gradle.plugin.processor.buddy)
}
processorBuddy {
//  mut
}
kotlin {
  sourceSets {
    commonMain.dependencies {
      // KSP 依赖
      implementation(libs.com.google.devtools.ksp.symbol.processing.api)

      // 基础工具
//            implementation(libs.site.addzero.addzero.ksp.support)

      // 实体分析支持
      implementation(libs.site.addzero.entity2analysed.support)

    }

    jvmMain.dependencies {
      // JVM 特定依赖
    }
  }
}

plugins {
  id("site.addzero.buildlogic.kmp.kmp-ksp")
  alias(libs.plugins.site.addzero.gradle.plugin.processor.buddy)
}

// 动态获取 shared 目录（通常为 src/commonMain/kotlin）
processorBuddy {
  val sharedDir = rootDir.resolve("shared/src/commonMain/kotlin").absolutePath
  packageName.set("site.addzero.entity2iso.processor.context")
  mustMap.set(
    mapOf(
      "isomorphicPkg" to "site.addzero.entity2iso.generated",
      "isomorphicGenDir" to sharedDir
    )
  )
}
kotlin {
  sourceSets {
    commonMain.dependencies {
      // KSP 依赖
      implementation(libs.com.google.devtools.ksp.symbol.processing.api)
      implementation(libs.androidx.room.compiler.processing)
      implementation(project(":lib:ksp:metadata:jimmer-entity-spi"))
    }

    jvmMain.dependencies {
      implementation(libs.androidx.room.compiler.processing)
    }
  }
}

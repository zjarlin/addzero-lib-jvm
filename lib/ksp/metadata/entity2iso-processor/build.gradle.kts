plugins {
  id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
  alias(libs.plugins.site.addzero.gradle.plugin.processor.buddy)
}
val catalogLibs = versionCatalogs.named("libs")

// 动态获取 shared 目录（通常为 src/commonMain/kotlin）
processorBuddy {
  val sharedDir = rootDir.resolve("shared/src/commonMain/kotlin").absolutePath
  packageName.set("site.addzero.entity2iso.processor.context")
  mustMap.set(
    mapOf(
      "isomorphicPkg" to "site.addzero.generated.isomorphic",
      "isomorphicGenDir" to sharedDir
    )
  )
}
kotlin {
  sourceSets {
    commonMain.dependencies {
      // KSP 依赖
      implementation(catalogLibs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
      implementation(catalogLibs.findLibrary("androidx-room-compiler-processing").get())
      implementation(project(":lib:ksp:metadata:jimmer-entity-spi"))
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
    }

    jvmMain.dependencies {
      implementation(catalogLibs.findLibrary("androidx-room-compiler-processing").get())
    }
  }
}

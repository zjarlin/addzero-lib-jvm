plugins {
  id("site.addzero.buildlogic.kmp.kmp-ksp")
  id("site.addzero.gradle.plugin.processor-buddy") version "+"
}
val libs = versionCatalogs.named("libs")

processorBuddy {
  packageName.set("site.addzero.route.processor.context")
  mustMap.set(
    mapOf(
      "sharedSourceDir" to "",
      "routeGenPkg" to "site.addzero.generated",
      "routeOwnerModule" to "",
      "routeAggregationRole" to "contributor",
    )
  )
}
kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.findLibrary("site-addzero-route-core").get())
      implementation(libs.findLibrary("site-addzero-tool-str").get())
    }
    jvmMain.dependencies {
      implementation(libs.findLibrary("site-addzero-tool-io-codegen").get())
    }
  }
}

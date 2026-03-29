plugins {
  id("site.addzero.buildlogic.kmp.kmp-ksp")
  id("site.addzero.gradle.plugin.processor-buddy") version "+"
}
val libs = versionCatalogs.named("libs")

processorBuddy {
  mustMap=mapOf(
    "sharedSourceDir" to "",
    "routeGenPkg" to "site.addzero.generated",
    "routeOwnerModule" to "",
    // KSP loads all processors in one classloader. Keep this Settings shape
    // aligned with controller2api-processor to avoid NoSuchMethodError.
    "apiClientPackageName" to "site.addzero.generated.api",
    "apiClientOutputDir" to ""
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

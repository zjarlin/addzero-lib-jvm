plugins {
  id("site.addzero.buildlogic.jvm.jvm-json")
  id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
  id("site.addzero.gradle.plugin.koin-convention")  version "2026.01.17"
}

dependencies {
  implementation(libs.okhttp)
  // KSP Singleton Adapter
  implementation(projects.lib.ksp.metadata.singletonAdapterApi)
  ksp(projects.lib.ksp.metadata.singletonAdapterProcessor)
  // Common models
  implementation(projects.lib.toolJvm.models.common.commonModels)

}

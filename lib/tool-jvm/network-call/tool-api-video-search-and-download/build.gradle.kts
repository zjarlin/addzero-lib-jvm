plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
  implementation(libs.okhttp)
  implementation(libs.fastjson2.kotlin)

  implementation(projects.lib.ksp.metadata.singletonAdapterApi)
  ksp(projects.lib.ksp.metadata.singletonAdapterProcessor)
}

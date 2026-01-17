plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
  id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")

}

dependencies {
  implementation(libs.okhttp)
  implementation(libs.fastjson2.kotlin)

  implementation(projects.lib.ksp.metadata.singletonAdapterApi)
  ksp(projects.lib.ksp.metadata.singletonAdapterProcessor)

}

plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
  id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")

}

dependencies {
  implementation(libs.okhttp)
  implementation(libs.fastjson2.kotlin)

  implementation("site.addzero:singleton-adapter-api:2026.01.20")
  ksp("site.addzero:singleton-adapter-processor:2026.01.20")

}

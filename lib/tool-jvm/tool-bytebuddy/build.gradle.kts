plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
  implementation(libs.byte.buddy)
  implementation(libs.tool.reflection)
}

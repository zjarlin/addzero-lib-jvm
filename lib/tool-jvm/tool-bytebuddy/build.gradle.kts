plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
  implementation(libs.net.bytebuddy.byte.buddy)
  implementation(libs.site.addzero.tool.reflection)
}

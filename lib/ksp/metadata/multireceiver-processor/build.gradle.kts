plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
  implementation(project(":lib:kcp:multireceiver:kcp-multireceiver-annotations"))
  implementation(libs.com.google.devtools.ksp.symbol.processing.api)
}

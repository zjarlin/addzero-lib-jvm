plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
  implementation(libs.findLibrary("singleton-adapter-api").get())
  implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")
}

plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}
dependencies {
  implementation(project(":lib:ksp:metadata:singleton-adapter-api"))
  implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")
}

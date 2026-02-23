plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")  version "+"
}
dependencies {
    api("site.addzero:lsi-core:2026.01.11")
    implementation(projects.jimmerCore)

    implementation(project(":ksp:jimmer-ksp-constants"))
}

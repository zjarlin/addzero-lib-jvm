plugins {
  id("site.addzero.buildlogic.jvm.jvm-json")
  id("site.addzero.buildlogic.jvm.jimmer")
}
val catalogLibs = versionCatalogs.named("libs")

dependencies {
  api(catalogLibs.findLibrary("org-babyfish-jimmer-jimmer-sql-kotlin").get())
  api(catalogLibs.findLibrary("org-jetbrains-kotlinx-kotlinx-serialization-json").get())

  kspTest(libs.org.babyfish.jimmer.jimmer.ksp)
}

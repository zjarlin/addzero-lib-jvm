plugins {
  id("site.addzero.buildlogic.jvm.jvm-json")
  id("site.addzero.buildlogic.jvm.jimmer")
}

dependencies {
  api(libs.org.babyfish.jimmer.jimmer.sql.kotlin)
  api(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)

  kspTest(libs.org.babyfish.jimmer.jimmer.ksp)
}

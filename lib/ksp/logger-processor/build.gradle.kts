plugins {
  id("site.addzero.buildlogic.jvm.jvm-ksp")
  id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")

}

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":lib:ksp:logger-api"))
  kspTest(project(":lib:ksp:logger-implementation"))
  kspTest(project(":lib:ksp:logger-processor"))
}


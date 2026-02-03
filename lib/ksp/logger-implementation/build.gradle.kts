plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")

}

repositories {
    mavenCentral()
}

dependencies {
  implementation(project(":lib:ksp:logger-api"))
    // Depend on the API module
}

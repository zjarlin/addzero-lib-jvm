plugins {
  id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
val libs = versionCatalogs.named("libs")

kotlin {

  sourceSets {
    commonMain.dependencies {
      implementation(libs.findLibrary("site-addzero-ksp-dsl-builder-core").get())

    }
    jvmMain.dependencies {

      implementation(libs.findLibrary("site-addzero-lsi-ksp").get())
      implementation(libs.findLibrary("cn-hutool-hutool-all").get())
    }
  }
}

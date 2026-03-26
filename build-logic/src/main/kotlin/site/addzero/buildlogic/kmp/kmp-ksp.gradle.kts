package site.addzero.buildlogic.kmp

plugins {
  id("site.addzero.buildlogic.kmp.kmp-convention")
}
val libs = versionCatalogs.named("libs")
dependencies {
  add("kspJvm", libs.findLibrary("dev-zacsweers-autoservice-auto-service-ksp").get())
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(versionCatalogs.named("libs").findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
    }
    jvmMain.dependencies {
      implementation(libs.findLibrary("com-google-auto-service-annotations").get())
    }
  }
}



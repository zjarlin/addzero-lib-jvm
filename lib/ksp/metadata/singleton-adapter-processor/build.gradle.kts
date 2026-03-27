plugins {
//  id("kmp-ksp")
  id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

kotlin {
  dependencies {
    implementation("site.addzero:singleton-adapter-api:2026.01.20")
  }
//  sourceSets.jvmMain.dependencies {
//    implementation(libs.findLibrary("com-squareup-kotlinpoet-ksp").get())
//  }
}

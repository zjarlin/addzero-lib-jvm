plugins {
//  id("kmp-ksp")
  id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

kotlin {
  dependencies {
    implementation(libs.findLibrary("singleton-adapter-api").get())
  }
//  sourceSets.jvmMain.dependencies {
//    implementation(libs.findLibrary("com-squareup-kotlinpoet-ksp").get())
//  }
}

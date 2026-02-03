plugins {
//  id("kmp-ksp")
  id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

kotlin {
  dependencies {
    implementation("site.addzero:singleton-adapter-api:2026.01.20")
  }
//  sourceSets.jvmMain.dependencies {
//    implementation(libs.kotlinpoet.ksp)
//  }
}

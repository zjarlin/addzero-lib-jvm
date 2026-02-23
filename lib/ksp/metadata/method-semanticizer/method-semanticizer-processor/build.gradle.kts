plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}

kotlin{
  dependencies {
//    implementation("site.addzero:method-semanticizer-api:2026.02.15")
    implementation("site.addzero:method-semanticizer-spi:2026.02.15")
    implementation(libs.com.google.devtools.ksp.symbol.processing.api)
    implementation(libs.com.squareup.kotlinpoet)
    implementation(libs.com.squareup.kotlinpoet.ksp)
  }

}

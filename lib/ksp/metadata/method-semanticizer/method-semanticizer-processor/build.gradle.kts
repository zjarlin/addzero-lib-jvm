plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
val libs = versionCatalogs.named("libs")

kotlin{
  dependencies {
//    implementation("site.addzero:method-semanticizer-api:2026.02.15")
    implementation("site.addzero:method-semanticizer-spi:2026.02.15")
    implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
    implementation(libs.findLibrary("com-squareup-kotlinpoet").get())
    implementation(libs.findLibrary("com-squareup-kotlinpoet-ksp").get())
  }

}

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}

kotlin{
  dependencies {
    implementation(project(":lib:ksp:metadata:method-semanticizer:method-semanticizer-api"))
    implementation(project(":lib:ksp:metadata:method-semanticizer:method-semanticizer-spi"))
    implementation(libs.com.google.devtools.ksp.symbol.processing.api)
    implementation(libs.com.squareup.kotlinpoet)
    implementation(libs.com.squareup.kotlinpoet.ksp)
  }

}

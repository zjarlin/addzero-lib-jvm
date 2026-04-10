plugins {
  id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}
val libs = versionCatalogs.named("libs")

dependencies {
  api(libs.findLibrary("site-addzero-lsi-core").get())
//  api("site.addzero:lsi-ksp:2026.02.26")
  compileOnly(libs.findLibrary("com-squareup-kotlinpoet").get())
//    compileOnly(libs.findLibrary("com-squareup-kotlinpoet-ksp").get())
}

description = "LSI 的 Jimmer 语义扩展层，提供 LsiClass/LsiField 的 ORM 语义扩展函数及 EntityMetadata 转换器"

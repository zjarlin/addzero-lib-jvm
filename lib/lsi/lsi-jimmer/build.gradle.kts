plugins {
  id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}

dependencies {
  api(project(":lib:lsi:lsi-core"))
  api(project(":lib:lsi:lsi-ksp"))
  compileOnly("com.squareup:kotlinpoet:2.2.0")
//    compileOnly(libs.kotlinpoet.ksp)
}

description = "LSI 的 Jimmer 语义扩展层，提供 LsiClass/LsiField 的 ORM 语义扩展函数及 EntityMetadata 转换器"

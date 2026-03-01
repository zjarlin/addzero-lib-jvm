plugins {
   id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}

dependencies {
    api(project(":checkouts:lsi:lsi-core"))
    api(project(":checkouts:lsi:lsi-ksp"))
    compileOnly("com.squareup:kotlinpoet:2.2.0")
//    compileOnly(libs.kotlinpoet.ksp)
}

description = "LSI 的 Jimmer 语义扩展层，提供 LsiClass/LsiField 的 ORM 语义扩展函数及 EntityMetadata 转换器"

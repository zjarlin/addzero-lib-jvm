plugins {
   id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("site-addzero-tool-str").get())
//    kotlin("stdlib")
}

description = "语言无关的不完备抽象层"

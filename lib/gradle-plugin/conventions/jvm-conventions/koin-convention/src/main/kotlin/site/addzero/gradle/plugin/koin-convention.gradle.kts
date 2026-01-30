package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
  id("site.addzero.gradle.plugin.kspplugin-convention")
}

val libs = the<LibrariesForLibs>()


dependencies {
  ksp(libs.koin.ksp.compiler)
//        implementation(platform("io.insert-koin:koin-bom:${extension.koinBomVersion.get()}"))
  implementation(platform(libs.koin.bom))
  implementation(libs.koin.core)
  implementation(libs.koin.annotations)
  implementation(libs.tool.koin)
}
ksp {
  arg("KOIN_DEFAULT_MODULE", "true")
}


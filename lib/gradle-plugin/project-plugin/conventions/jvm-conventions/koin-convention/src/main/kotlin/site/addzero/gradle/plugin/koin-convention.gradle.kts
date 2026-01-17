package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import site.addzero.gradle.KoinConventionExtension

plugins {
  id("site.addzero.gradle.plugin.kspplugin-convention")
//  id("com.google.devtools.ksp")
}

val extension = extensions.create<KoinConventionExtension>("koinConvention")
val libs = the<LibrariesForLibs>()

afterEvaluate {

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

}

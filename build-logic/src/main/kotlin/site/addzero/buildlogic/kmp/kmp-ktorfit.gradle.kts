package site.addzero.buildlogic.kmp

plugins {
  id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
  id("site.addzero.buildlogic.kmp.kmp-core")
  id("de.jensklingenberg.ktorfit")
}

val libs = versionCatalogs.named("libs")
val ktorfitCompilerVersion = libs.findVersion("ktorfit-compiler-plugin-version").get().requiredVersion

ktorfit {
    compilerPluginVersion = ktorfitCompilerVersion
}

kotlin {
  dependencies {
    implementation(versionCatalogs.named("libs").findLibrary("de-jensklingenberg-ktorfit-ktorfit-lib").get())
  }
}

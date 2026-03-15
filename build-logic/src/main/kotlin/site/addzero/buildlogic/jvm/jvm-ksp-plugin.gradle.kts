package site.addzero.buildlogic.jvm


plugins {
    id("com.google.devtools.ksp")
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val libs = versionCatalogs.named("libs")
val kspJvmGeneratedPath = libs.findVersion("ksp-jvm-generated-path")
  .orElse(null)
  ?.requiredVersion
  ?: "build/generated/ksp/main/kotlin"

kotlin {
    sourceSets {
        main {
            kotlin.srcDir(kspJvmGeneratedPath)
        }
    }

}

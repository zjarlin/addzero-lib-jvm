package site.addzero.buildlogic.kmp

plugins {
    id("com.google.devtools.ksp")
    id("site.addzero.buildlogic.ksp.ksp-task-dependencies")
    id("site.addzero.buildlogic.kmp.kmp-convention")
}

val libs = versionCatalogs.named("libs")
val kspCommonGeneratedPath = libs.findVersion("ksp-common-generated-path").get().requiredVersion

kotlin {
    sourceSets.commonMain {
        kotlin.srcDir(kspCommonGeneratedPath)
    }
}


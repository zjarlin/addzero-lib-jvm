package site.addzero.buildlogic.kmp

plugins {
    id("site.addzero.buildlogic.kmp.kmp-convention")
    id("io.insert-koin.compiler.plugin")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.findLibrary("io-insert-koin-koin-bom").get()))
            implementation(libs.findLibrary("io-insert-koin-koin-annotations").get())
            implementation(libs.findLibrary("io-insert-koin-koin-core").get())
        }
    }
}

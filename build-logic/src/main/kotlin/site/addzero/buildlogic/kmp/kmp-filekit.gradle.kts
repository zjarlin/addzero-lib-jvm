package site.addzero.buildlogic.kmp

plugins {
    id("site.addzero.buildlogic.kmp.kmp-convention")
}

val libs = versionCatalogs.named("libs")

kotlin {
    dependencies {
        implementation(libs.findLibrary("io-github-vinceglb-filekit-core").get())
        implementation(libs.findLibrary("io-github-vinceglb-filekit-dialogs").get())
        implementation(libs.findLibrary("io-github-vinceglb-filekit-dialogs-compose").get())
        implementation(libs.findLibrary("io-github-vinceglb-filekit-coil").get())
    }
}

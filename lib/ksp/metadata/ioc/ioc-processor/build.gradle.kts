plugins {
    id("site.addzero.gradle.plugin.kmp-ksp-convention")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation(libs.addzero.ksp.support)
            implementation(libs.addzero.ioc.core)
            implementation(libs.lsi.ksp)
        }
    }
}

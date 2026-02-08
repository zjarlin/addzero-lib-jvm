plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
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

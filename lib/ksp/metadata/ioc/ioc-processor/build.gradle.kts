plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation(libs.addzero.ksp.support)
            implementation(libs.ioc.core)
            implementation(libs.lsi.ksp)
        }
    }
}

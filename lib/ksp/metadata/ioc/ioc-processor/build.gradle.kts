plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation(libs.site.addzero.addzero.ksp.support)
            implementation(libs.site.addzero.ioc.core)
            implementation(libs.site.addzero.lsi.ksp)
        }
    }
}

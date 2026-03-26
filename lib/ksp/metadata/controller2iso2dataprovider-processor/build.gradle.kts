plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.site.addzero.ksp.support)
        }
    }
}

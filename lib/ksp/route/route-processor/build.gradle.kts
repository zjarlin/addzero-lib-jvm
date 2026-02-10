plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.site.addzero.addzero.route.core)

            implementation(libs.site.addzero.addzero.ksp.support)
        }
    }
}

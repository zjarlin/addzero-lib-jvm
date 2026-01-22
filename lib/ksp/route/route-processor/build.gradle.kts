plugins {
    id("site.addzero.buildlogic.kmp.libs.kmp-ksp")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.addzero.route.core)

            implementation(libs.addzero.ksp.support)
        }
    }
}

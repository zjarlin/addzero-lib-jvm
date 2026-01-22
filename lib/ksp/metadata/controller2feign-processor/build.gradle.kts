plugins {
    id("site.addzero.buildlogic.kmp.libs.kmp-ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.addzero.ksp.support)
        }
    }
}

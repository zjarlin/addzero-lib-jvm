plugins {
    id("kmp-ksp")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.addzero.ksp.support)
            implementation(libs.hutool.all)
        }
    }
}

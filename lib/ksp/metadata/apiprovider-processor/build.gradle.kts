plugins {
    id("kmp-ksp")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("site.addzero:addzero-ksp-support:2025.09.29")
        }
    }
}

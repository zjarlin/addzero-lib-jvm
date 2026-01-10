plugins {
    id("kmp-ksp")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation("site.addzero:addzero-ksp-support:2025.09.29")
            implementation("site.addzero:addzero-ioc-core:2025.09.29")
            implementation("site.addzero:lsi-ksp:2026.01.11")
        }
    }
}

plugins {
    id("kmp-ksp")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            api("site.addzero:addzero-route-core:2025.09.29")

            implementation("site.addzero:addzero-ksp-support:2025.09.29")
        }
    }
}

plugins {
    id("kmp-ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("site.addzero:addzero-ksp-support-jdbc:2025.09.29")
            implementation("site.addzero:addzero-ksp-support:2025.09.29")
            implementation("site.addzero:tool-jdbc-model:2025.09.30")

        }

    }
}

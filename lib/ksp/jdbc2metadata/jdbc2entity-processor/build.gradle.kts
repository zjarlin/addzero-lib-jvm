plugins {
    id("kmp-ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // 基础工具
            implementation("site.addzero:addzero-ksp-support:2025.09.29")
            implementation("site.addzero:addzero-ksp-support-jdbc:2025.09.29")

        }
    }
}

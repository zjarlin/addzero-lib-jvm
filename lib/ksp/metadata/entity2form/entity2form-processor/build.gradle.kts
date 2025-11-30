plugins {
    id("kmp-ksp")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("site.addzero:addzero-ksp-support:2025.09.29")
            implementation("site.addzero:addzero-entity2form-core:2025.09.29")

            // 实体分析支持
            implementation("site.addzero:addzero-entity2analysed-support:2025.09.29")

        }
    }
}

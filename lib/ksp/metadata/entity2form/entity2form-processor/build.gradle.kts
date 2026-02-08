plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.addzero.ksp.support)
            implementation(libs.addzero.entity2form.core)

            // 实体分析支持
            implementation(libs.addzero.entity2analysed.support)

        }
    }
}

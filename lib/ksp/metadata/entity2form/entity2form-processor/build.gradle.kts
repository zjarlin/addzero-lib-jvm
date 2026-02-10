plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.site.addzero.addzero.ksp.support)
            implementation(libs.entity2form.core)

            // 实体分析支持
            implementation(libs.site.addzero.addzero.entity2analysed.support)

        }
    }
}

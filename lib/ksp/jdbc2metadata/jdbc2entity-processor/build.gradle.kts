plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // 基础工具
            implementation(libs.site.addzero.addzero.ksp.support)
            implementation(libs.site.addzero.addzero.ksp.support.jdbc)

        }
    }
}

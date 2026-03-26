plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // 基础工具
            implementation(libs.site.addzero.ksp.support)
            implementation(libs.site.addzero.ksp.support.jdbc)

        }
    }
}

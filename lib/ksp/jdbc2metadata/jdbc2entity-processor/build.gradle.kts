plugins {
    id("site.addzero.gradle.plugin.kmp-ksp-convention")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // 基础工具
            implementation(libs.addzero.ksp.support)
            implementation(libs.addzero.ksp.support.jdbc)

        }
    }
}

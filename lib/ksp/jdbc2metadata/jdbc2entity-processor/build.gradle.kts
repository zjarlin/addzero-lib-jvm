plugins {
    id("kmp-ksp")
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

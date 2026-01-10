plugins {
    id("kmp-ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // KSP 依赖
            implementation(libs.ksp.symbol.processing.api)

            // 基础工具
            implementation(libs.addzero.ksp.support)

            // 实体分析支持
            implementation(libs.addzero.entity2analysed.support)

        }

        jvmMain.dependencies {
            // JVM 特定依赖
        }
    }
}

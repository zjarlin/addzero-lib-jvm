plugins {
    id("kmp-ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // KSP 依赖
            implementation(libs.ksp.symbol.processing.api)

            // 基础工具
            implementation(projects.lib.ksp.common.kspSupport)

            // 实体分析支持
            implementation(projects.lib.ksp.metadata.entity2analysedSupport)

        }

        jvmMain.dependencies {
            // JVM 特定依赖
        }
    }
}

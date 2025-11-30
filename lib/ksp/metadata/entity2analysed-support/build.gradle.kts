plugins {
    id("kmp-ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // KSP 依赖
//            implementation(libs.ksp.symbol.processing.api)

            // 基础工具
            implementation("site.addzero:addzero-ksp-support:2025.09.29")


//            implementation(projects.lib.kld.addzeroKaleidoscopeKsp)

        }

        jvmMain.dependencies {
            // JVM 特定依赖
        }
    }
}

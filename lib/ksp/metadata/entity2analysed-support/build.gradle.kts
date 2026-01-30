plugins {
    id("site.addzero.gradle.plugin.kmp-ksp-convention")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // KSP 依赖
//            implementation(libs.ksp.symbol.processing.api)

            // 基础工具
            implementation(libs.addzero.ksp.support)


//            implementation(projects.lib.kld.addzeroKaleidoscopeKsp)

        }

        jvmMain.dependencies {
            // JVM 特定依赖
        }
    }
}

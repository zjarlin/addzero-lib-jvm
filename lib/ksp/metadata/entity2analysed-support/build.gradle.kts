plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // KSP 依赖
//            implementation(libs.com.google.devtools.ksp.symbol.processing.api)

            // 基础工具
            implementation(libs.site.addzero.addzero.ksp.support)


//            implementation(projects.lib.kld.addzeroKaleidoscopeKsp)

        }

        jvmMain.dependencies {
            // JVM 特定依赖
        }
    }
}

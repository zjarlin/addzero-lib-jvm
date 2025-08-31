plugins {
    id("kmp-ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // 基础工具
            implementation(projects.lib.ksp.common.addzeroKspSupport)
            implementation(projects.lib.ksp.common.addzeroKspSupportJdbc)

        }
    }
}

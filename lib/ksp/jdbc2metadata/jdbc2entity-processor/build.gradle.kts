plugins {
    id("kmp-ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // 基础工具
            implementation(projects.lib.ksp.common.kspSupport)
            implementation(projects.lib.ksp.common.kspSupportJdbc)

        }
    }
}

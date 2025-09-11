plugins {
    id("kmp-ksp")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            // 基础工具
            implementation(projects.lib.ksp.common.addzeroKspSupport)

            // 实体分析支持
            implementation(projects.lib.ksp.metadata.addzeroEntity2analysedSupport)



        }
    }
}

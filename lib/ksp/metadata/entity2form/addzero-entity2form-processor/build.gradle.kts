plugins {
    id("kmp-ksp")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.ksp.common.addzeroKspSupport)
            implementation(projects.lib.ksp.metadata.entity2form.addzeroEntity2formCore)

            // 实体分析支持
            implementation(projects.lib.ksp.metadata.addzeroEntity2analysedSupport)
        }
    }
}

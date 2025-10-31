plugins {
    id("kmp-ksp")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.ksp.common.kspSupport)
            implementation(projects.lib.ksp.metadata.entity2form.entity2formCore)

            // 实体分析支持
            implementation(projects.lib.ksp.metadata.entity2analysedSupport)

        }
    }
}

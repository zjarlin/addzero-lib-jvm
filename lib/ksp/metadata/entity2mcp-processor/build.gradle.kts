plugins {
    id("site.addzero.gradle.plugin.kmp-ksp-convention")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            // 基础工具
            implementation(libs.addzero.ksp.support)

            // 实体分析支持
            implementation(libs.addzero.entity2analysed.support)




        }
    }
}

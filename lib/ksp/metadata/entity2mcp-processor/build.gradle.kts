plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // 基础工具
            implementation(libs.site.addzero.ksp.support)
            implementation(libs.androidx.room.compiler.processing)
            implementation(project(":lib:ksp:metadata:jimmer-entity-spi"))
        }
    }
}

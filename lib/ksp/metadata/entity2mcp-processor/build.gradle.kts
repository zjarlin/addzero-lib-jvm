plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            // 基础工具
            implementation(libs.findLibrary("site-addzero-ksp-support").get())
            implementation(libs.findLibrary("androidx-room-compiler-processing").get())
            implementation(project(":lib:ksp:metadata:jimmer-entity-spi"))
        }
    }
}

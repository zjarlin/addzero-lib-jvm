plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            // 基础工具
            implementation(project(":lib:ksp:common:ksp-support"))
            implementation(libs.findLibrary("androidx-room-compiler-processing").get())
            implementation(project(":lib:ksp:metadata:jimmer-entity-spi"))
        }
    }
}

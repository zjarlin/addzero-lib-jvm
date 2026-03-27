plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("site-addzero-ksp-support").get())
            implementation(libs.findLibrary("androidx-room-compiler-processing").get())
            implementation(libs.findLibrary("site-addzero-entity2form-core").get())
            implementation(project(":lib:ksp:metadata:jimmer-entity-spi"))
        }
    }
}

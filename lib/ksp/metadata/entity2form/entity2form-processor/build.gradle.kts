plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:ksp:common:ksp-support"))
            implementation(libs.findLibrary("androidx-room-compiler-processing").get())
            implementation(libs.findLibrary("site-addzero-entity2form-core").get())
            implementation(project(":lib:ksp:metadata:jimmer-entity-spi"))
        }
    }
}

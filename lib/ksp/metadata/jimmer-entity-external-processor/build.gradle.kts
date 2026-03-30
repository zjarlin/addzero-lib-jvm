plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:ksp:metadata:jimmer-entity-spi"))
            implementation(project(":lib:tool-kmp:tool-coll"))
            implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-core").get())
            implementation(libs.findLibrary("androidx-room-compiler-processing").get())

            implementation(project(":lib:ksp:metadata:entity2form:entity2form-processor"))
            implementation(project(":lib:ksp:metadata:entity2iso-processor"))
            implementation(project(":lib:ksp:metadata:entity2mcp-processor"))
        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

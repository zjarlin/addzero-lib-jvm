plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:ksp:metadata:jimmer-entity-spi"))
            implementation(project(":lib:tool-kmp:tool-coll"))
            implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
            implementation(libs.androidx.room.compiler.processing)

            implementation(project(":lib:ksp:metadata:entity2form:entity2form-processor"))
            implementation(project(":lib:ksp:metadata:entity2iso-processor"))
            implementation(project(":lib:ksp:metadata:entity2mcp-processor"))
        }
    }
}

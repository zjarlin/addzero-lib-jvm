plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.gradle.plugin.processor-buddy") version "+"
}
val libs = versionCatalogs.named("libs")

processorBuddy {
    packageName.set("site.addzero.entity2mcp.processor.context")
    mustMap.set(
        mapOf(
            "backendServerSourceDir" to "",
            "mcpPackageName" to "site.addzero.generated.mcp",
            "isomorphicPackageName" to "site.addzero.generated.isomorphic",
            "isomorphicClassSuffix" to "Iso",
        )
    )
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("androidx-room-compiler-processing").get())
            implementation(project(":lib:ksp:metadata:jimmer-entity-spi"))
        }
    }
}

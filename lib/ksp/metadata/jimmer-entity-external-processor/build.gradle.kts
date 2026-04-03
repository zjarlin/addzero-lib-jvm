plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    alias(libs.plugins.site.addzero.gradle.plugin.processor.buddy)
}
val libs = versionCatalogs.named("libs")
val defaultSharedSourceDir = rootDir.resolve("shared/src/commonMain/kotlin").absolutePath.replace('\\', '/')
val defaultIsoPackageName = "site.addzero.generated.isomorphic"

processorBuddy {
    packageName.set("site.addzero.jimmer.entity.external.processor.context")
    mustMap.set(
        linkedMapOf(
            "isomorphicPkg" to defaultIsoPackageName,
            "isomorphicGenDir" to "$defaultSharedSourceDir/${defaultIsoPackageName.replace(".", "/")}",
            "sharedSourceDir" to defaultSharedSourceDir,
            "sharedComposeSourceDir" to defaultSharedSourceDir,
            "backendServerSourceDir" to "",
            "isomorphicPackageName" to defaultIsoPackageName,
            "isomorphicClassSuffix" to "Iso",
            "isomorphicSerializableEnabled" to "true",
            "entity2Iso.enabled" to "true",
            "entity2Form.enabled" to "true",
            "entity2Mcp.enabled" to "true",
            "formPackageName" to "site.addzero.generated.forms",
            "enumOutputPackage" to "site.addzero.generated.enums",
            "apiClientPackageName" to "site.addzero.generated.api",
            "iso2DataProviderPackage" to "site.addzero.generated.forms.dataprovider",
            "mcpPackageName" to "site.addzero.generated.mcp",
        )
    )
}

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

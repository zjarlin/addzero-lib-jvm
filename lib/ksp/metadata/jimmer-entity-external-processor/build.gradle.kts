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
            implementation(libs.findLibrary("jimmer-entity-spi").get())
            implementation(libs.findLibrary("site-addzero-tool-coll").get())
            implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-core").get())
            implementation(libs.findLibrary("androidx-room-compiler-processing").get())

            implementation(libs.findLibrary("site-addzero-entity2form-processor").get())
            implementation(libs.findLibrary("site-addzero-entity2iso-processor").get())
            implementation(libs.findLibrary("site-addzero-entity2mcp-processor").get())
        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

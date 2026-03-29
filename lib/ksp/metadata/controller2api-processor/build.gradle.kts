plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
    alias(libs.plugins.site.addzero.gradle.plugin.processor.buddy)
}
val libs = versionCatalogs.named("libs")
val defaultApiClientPackageName = "site.addzero.generated.api"
val defaultSharedComposeDir = rootDir.resolve("shared/src/commonMain/kotlin")

processorBuddy {
    packageName.set("site.addzero.controller2api.processor.context")
    mustMap.set(
        mapOf(
            "apiClientPackageName" to defaultApiClientPackageName,
            "apiClientOutputDir" to defaultSharedComposeDir
                .resolve(defaultApiClientPackageName.replace(".", "/"))
                .absolutePath,
        )
    )
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
        }
    }
}

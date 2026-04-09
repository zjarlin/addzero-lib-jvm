import org.gradle.declarative.dsl.schema.FqName.Empty.packageName

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
    alias(libs.plugins.site.addzero.gradle.plugin.processor.buddy)
}
val libs = versionCatalogs.named("libs")
val defaultApiClientPackageName = "site.addzero.generated.api.generated"
val defaultGeneratedSourceDir = rootDir.resolve("build/generated/source/controller2api/commonMain/kotlin")

processorBuddy {
    packageName.set("site.addzero.controller2api.processor.context")
    mustMap.set(
        mapOf(
            "apiClientPackageName" to defaultApiClientPackageName,
            "apiClientAggregatorObjectName" to "Apis",
            "apiClientAggregatorStyle" to "koin",
            "apiClientAggregatorOutputDir" to defaultGeneratedSourceDir
                .resolve(defaultApiClientPackageName.replace(".", "/"))
                .absolutePath.replace('\\', '/'),
            "apiClientOutputDir" to defaultGeneratedSourceDir
                .resolve(defaultApiClientPackageName.replace(".", "/"))
                .absolutePath.replace('\\', '/'),
        )
    )
}

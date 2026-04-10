plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
    alias(libs.plugins.site.addzero.gradle.plugin.processor.buddy)
}

//version = "2026.04.10"

val libs = versionCatalogs.named("libs")
val defaultApiClientPackageName = "site.addzero.generated.api.generated"
val defaultGeneratedSourceDir = rootDir.resolve("build/generated/source/controller2api/commonMain/kotlin")

processorBuddy {
    packageName.set("site.addzero.controller2api.processor.context")
    mustMap.set(
        mapOf(
            "apiClientPackageName" to defaultApiClientPackageName,
            // 留空时沿用 apiClientOutputDir。
            "apiClientAggregatorOutputDir" to "",
            // 留空时处理器默认生成同包聚合对象 `Apis`。
            "apiClientAggregatorObjectName" to "",
            "apiClientAggregatorStyle" to "koin",
            "apiClientOutputDir" to defaultGeneratedSourceDir
                .resolve(defaultApiClientPackageName.replace(".", "/"))
                .absolutePath.replace('\\', '/'),
        )
    )
}

version = "2026.04.13"

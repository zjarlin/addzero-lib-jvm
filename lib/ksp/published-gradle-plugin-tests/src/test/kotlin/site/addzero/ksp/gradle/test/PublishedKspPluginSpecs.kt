package site.addzero.ksp.gradle.test

data class PublishedKspPluginSpec(
    val pluginId: String,
    val processorArtifactId: String,
    val resourcePath: String,
    val extensionName: String? = null,
    val defaultValues: Map<String, Any?> = emptyMap(),
)

object PublishedKspPluginSpecs {
    val retained: List<PublishedKspPluginSpec> = listOf(
        PublishedKspPluginSpec(
            pluginId = "site.addzero.ksp.compose-props",
            processorArtifactId = "compose-props-processor",
            resourcePath = "site/addzero/ksp/compose-props/gradle-plugin.properties",
            extensionName = "composeProps",
            defaultValues = mapOf(
                "suffix" to "State",
            ),
        ),
        PublishedKspPluginSpec(
            pluginId = "site.addzero.ksp.gen-reified",
            processorArtifactId = "gen-reified-processor",
            resourcePath = "site/addzero/ksp/gen-reified/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            pluginId = "site.addzero.ksp.ioc",
            processorArtifactId = "ioc-processor",
            resourcePath = "site/addzero/ksp/ioc/gradle-plugin.properties",
            extensionName = "ioc",
            defaultValues = mapOf(
                "modulePackage" to "",
                "app" to false,
            ),
        ),
        PublishedKspPluginSpec(
            pluginId = "site.addzero.ksp.jimmer-entity-external",
            processorArtifactId = "jimmer-entity-external-processor",
            resourcePath = "site/addzero/ksp/jimmer-entity-external/gradle-plugin.properties",
            extensionName = "jimmerEntityExternal",
            defaultValues = mapOf(
                "sharedSourceDir" to "",
                "sharedComposeSourceDir" to "",
                "backendServerSourceDir" to "",
                "apiClientPackageName" to "site.addzero.generated.api",
                "enumOutputPackage" to "site.addzero.generated.enums",
                "iso2DataProviderPackage" to "site.addzero.generated.forms.dataprovider",
                "entity2Iso.enabled" to true,
                "entity2Iso.packageName" to "site.addzero.generated.isomorphic",
                "entity2Iso.classSuffix" to "Iso",
                "entity2Iso.serializableEnabled" to true,
                "entity2Form.enabled" to true,
                "entity2Form.packageName" to "site.addzero.generated.forms",
                "entity2Mcp.enabled" to true,
                "entity2Mcp.packageName" to "site.addzero.generated.mcp",
            ),
        ),
        PublishedKspPluginSpec(
            pluginId = "site.addzero.ksp.ksp-dsl-builder",
            processorArtifactId = "ksp-dsl-builder-processor",
            resourcePath = "site/addzero/ksp/ksp-dsl-builder/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            pluginId = "site.addzero.ksp.method-semanticizer",
            processorArtifactId = "method-semanticizer-processor",
            resourcePath = "site/addzero/ksp/method-semanticizer/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            pluginId = "site.addzero.ksp.modbus-rtu",
            processorArtifactId = "modbus-ksp-rtu",
            resourcePath = "site/addzero/ksp/modbus-rtu/gradle-plugin.properties",
            extensionName = "modbusRtu",
            defaultValues = mapOf(
                "codegenModes" to listOf("server"),
                "contractPackages" to emptyList<String>(),
                "transports" to listOf("rtu"),
                "cOutputProjectDir" to "",
                "bridgeImplPath" to "",
                "keilUvprojxPath" to "",
                "keilTargetName" to "",
                "keilGroupName" to "Core/modbus/rtu",
                "mxprojectPath" to "",
                "springRouteOutputDir" to "",
            ),
        ),
        PublishedKspPluginSpec(
            pluginId = "site.addzero.ksp.modbus-tcp",
            processorArtifactId = "modbus-ksp-tcp",
            resourcePath = "site/addzero/ksp/modbus-tcp/gradle-plugin.properties",
            extensionName = "modbusTcp",
            defaultValues = mapOf(
                "codegenModes" to listOf("server"),
                "contractPackages" to emptyList<String>(),
                "transports" to listOf("tcp"),
                "cOutputProjectDir" to "",
                "bridgeImplPath" to "",
                "keilUvprojxPath" to "",
                "keilTargetName" to "",
                "keilGroupName" to "Core/modbus/tcp",
                "mxprojectPath" to "",
                "springRouteOutputDir" to "",
            ),
        ),
        PublishedKspPluginSpec(
            pluginId = "site.addzero.ksp.multireceiver",
            processorArtifactId = "multireceiver-processor",
            resourcePath = "site/addzero/ksp/multireceiver/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            pluginId = "site.addzero.ksp.singleton-adapter",
            processorArtifactId = "singleton-adapter-processor",
            resourcePath = "site/addzero/ksp/singleton-adapter/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            pluginId = "site.addzero.ksp.spring2ktor-server",
            processorArtifactId = "spring2ktor-server-processor",
            resourcePath = "site/addzero/ksp/spring2ktor-server/gradle-plugin.properties",
            extensionName = "spring2ktorServer",
            defaultValues = mapOf(
                "generatedPackage" to "",
            ),
        ),
        PublishedKspPluginSpec(
            pluginId = "site.addzero.ksp.route",
            processorArtifactId = "route-processor",
            resourcePath = "site/addzero/ksp/route/gradle-plugin.properties",
            extensionName = "route",
            defaultValues = mapOf(
                "sharedSourceDir" to "",
                "generatedPackage" to "site.addzero.generated",
                "routeOwnerModule" to "",
                "moduleKey" to "",
            ),
        ),
    )

    val removed: Set<String> = setOf(
        "site.addzero.ksp.jdbc2controller",
        "site.addzero.ksp.jdbc2entity",
        "site.addzero.ksp.jdbc2enum",
        "site.addzero.ksp.controller2api",
        "site.addzero.ksp.controller2feign",
        "site.addzero.ksp.controller2iso2dataprovider",
        "site.addzero.ksp.enum",
    )

    val loggerDemo: Set<String> = setOf("site.addzero.ksp.logger")

    val spiOnly: Set<String> = setOf(
        "entity2iso-processor",
        "entity2form-processor",
        "entity2mcp-processor",
        "jimmer-entity-spi",
    )
}

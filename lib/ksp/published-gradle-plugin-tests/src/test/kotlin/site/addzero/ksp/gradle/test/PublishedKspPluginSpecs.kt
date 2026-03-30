package site.addzero.ksp.gradle.test

data class PublishedKspPluginSpec(
    val implementationClass: String,
    val pluginId: String,
    val processorArtifactId: String,
    val resourcePath: String,
    val extensionName: String? = null,
    val defaultValues: Map<String, Any?> = emptyMap(),
    val verifyExtensionDefaults: Boolean = false,
)

object PublishedKspPluginSpecs {
    val all: List<PublishedKspPluginSpec> = listOf(
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.jdbc2controller.gradle.Jdbc2ControllerGradlePlugin",
            pluginId = "site.addzero.ksp.jdbc2controller",
            processorArtifactId = "jdbc2controller-processor",
            resourcePath = "site/addzero/ksp/jdbc2controller/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.jdbc2entity.gradle.Jdbc2EntityGradlePlugin",
            pluginId = "site.addzero.ksp.jdbc2entity",
            processorArtifactId = "jdbc2entity-processor",
            resourcePath = "site/addzero/ksp/jdbc2entity/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.jdbc2enum.gradle.Jdbc2EnumGradlePlugin",
            pluginId = "site.addzero.ksp.jdbc2enum",
            processorArtifactId = "jdbc2enum-processor",
            resourcePath = "site/addzero/ksp/jdbc2enum/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.logger.gradle.LoggerGradlePlugin",
            pluginId = "site.addzero.ksp.logger",
            processorArtifactId = "logger-processor",
            resourcePath = "site/addzero/ksp/logger/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.apiprovider.gradle.ApiProviderGradlePlugin",
            pluginId = "site.addzero.ksp.apiprovider",
            processorArtifactId = "apiprovider-processor",
            resourcePath = "site/addzero/ksp/apiprovider/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.composeprops.gradle.ComposePropsGradlePlugin",
            pluginId = "site.addzero.ksp.compose-props",
            processorArtifactId = "compose-props-processor",
            resourcePath = "site/addzero/ksp/compose-props/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.controller2api.gradle.Controller2ApiGradlePlugin",
            pluginId = "site.addzero.ksp.controller2api",
            processorArtifactId = "controller2api-processor",
            resourcePath = "site/addzero/ksp/controller2api/gradle-plugin.properties",
            extensionName = "controller2api",
            defaultValues = mapOf(
                "generatedPackage" to "site.addzero.generated.api",
                "outputDir" to "",
            ),
            verifyExtensionDefaults = false,
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.controller2feign.gradle.Controller2FeignGradlePlugin",
            pluginId = "site.addzero.ksp.controller2feign",
            processorArtifactId = "controller2feign-processor",
            resourcePath = "site/addzero/ksp/controller2feign/gradle-plugin.properties",
            extensionName = "controller2feign",
            defaultValues = mapOf(
                "outputPackage" to "site.addzero.generated.feign",
                "outputDir" to "",
                "enabled" to true,
            ),
            verifyExtensionDefaults = false,
        ),
        PublishedKspPluginSpec(
            implementationClass =
                "site.addzero.ksp.controller2iso2dataprovider.gradle.Controller2Iso2DataProviderGradlePlugin",
            pluginId = "site.addzero.ksp.controller2iso2dataprovider",
            processorArtifactId = "controller2iso2dataprovider-processor",
            resourcePath = "site/addzero/ksp/controller2iso2dataprovider/gradle-plugin.properties",
            extensionName = "controller2iso2dataprovider",
            defaultValues = mapOf(
                "sharedComposeSourceDir" to "",
                "generatedPackage" to "site.addzero.generated.forms.dataprovider",
                "apiClientPackageName" to "site.addzero.generated.api",
                "isomorphicPackageName" to "site.addzero.generated.isomorphic",
            ),
            verifyExtensionDefaults = false,
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.enumprocessor.gradle.EnumGradlePlugin",
            pluginId = "site.addzero.ksp.enum",
            processorArtifactId = "enum-processor",
            resourcePath = "site/addzero/ksp/enum/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.genreified.gradle.GenReifiedGradlePlugin",
            pluginId = "site.addzero.ksp.gen-reified",
            processorArtifactId = "gen-reified-processor",
            resourcePath = "site/addzero/ksp/gen-reified/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.ioc.gradle.IocGradlePlugin",
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
            implementationClass =
                "site.addzero.ksp.jimmerentityexternal.gradle.JimmerEntityExternalGradlePlugin",
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
            implementationClass = "site.addzero.ksp.kspdslbuilder.gradle.KspDslBuilderGradlePlugin",
            pluginId = "site.addzero.ksp.ksp-dsl-builder",
            processorArtifactId = "ksp-dsl-builder-processor",
            resourcePath = "site/addzero/ksp/ksp-dsl-builder/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.methodsemanticizer.gradle.MethodSemanticizerGradlePlugin",
            pluginId = "site.addzero.ksp.method-semanticizer",
            processorArtifactId = "method-semanticizer-processor",
            resourcePath = "site/addzero/ksp/method-semanticizer/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.modbusrtu.gradle.ModbusRtuGradlePlugin",
            pluginId = "site.addzero.ksp.modbus-rtu",
            processorArtifactId = "modbus-ksp-rtu",
            resourcePath = "site/addzero/ksp/modbus-rtu/gradle-plugin.properties",
            extensionName = "modbusRtu",
            defaultValues = mapOf(
                "codegenModes" to listOf("server"),
                "contractPackages" to emptyList<String>(),
            ),
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.modbustcp.gradle.ModbusTcpGradlePlugin",
            pluginId = "site.addzero.ksp.modbus-tcp",
            processorArtifactId = "modbus-ksp-tcp",
            resourcePath = "site/addzero/ksp/modbus-tcp/gradle-plugin.properties",
            extensionName = "modbusTcp",
            defaultValues = mapOf(
                "codegenModes" to listOf("server"),
                "contractPackages" to emptyList<String>(),
            ),
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.multireceiver.gradle.MultireceiverGradlePlugin",
            pluginId = "site.addzero.ksp.multireceiver",
            processorArtifactId = "multireceiver-processor",
            resourcePath = "site/addzero/ksp/multireceiver/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.singletonadapter.gradle.SingletonAdapterGradlePlugin",
            pluginId = "site.addzero.ksp.singleton-adapter",
            processorArtifactId = "singleton-adapter-processor",
            resourcePath = "site/addzero/ksp/singleton-adapter/gradle-plugin.properties",
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.spring2ktorserver.gradle.Spring2KtorServerGradlePlugin",
            pluginId = "site.addzero.ksp.spring2ktor-server",
            processorArtifactId = "spring2ktor-server-processor",
            resourcePath = "site/addzero/ksp/spring2ktor-server/gradle-plugin.properties",
            extensionName = "spring2ktorServer",
            defaultValues = mapOf(
                "generatedPackage" to "",
            ),
        ),
        PublishedKspPluginSpec(
            implementationClass = "site.addzero.ksp.route.gradle.RouteGradlePlugin",
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
}

package site.addzero.device.protocol.modbus.ksp.tcp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.io.File
import site.addzero.device.protocol.modbus.ksp.core.CollectedModbusService
import site.addzero.device.protocol.modbus.ksp.core.ModbusAddressLockFile
import site.addzero.device.protocol.modbus.ksp.core.ModbusAddressPlanner
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactRenderer
import site.addzero.device.protocol.modbus.ksp.core.ModbusCodegenMode
import site.addzero.device.protocol.modbus.ksp.core.ModbusExternalCArtifactWriter
import site.addzero.device.protocol.modbus.ksp.core.ModbusMetadataCollector
import site.addzero.device.protocol.modbus.ksp.core.ModbusModelValidator
import site.addzero.device.protocol.modbus.ksp.core.ModbusProjectSyncRunner
import site.addzero.device.protocol.modbus.ksp.core.ModbusServerRouteMode
import site.addzero.device.protocol.modbus.ksp.core.ModbusTransportKind
import site.addzero.device.protocol.modbus.ksp.core.resolveContractPackages
import site.addzero.device.protocol.modbus.ksp.core.resolveEnabledTransports
import site.addzero.device.protocol.modbus.ksp.core.resolveApiClientOutputDir
import site.addzero.device.protocol.modbus.ksp.core.resolveApiClientPackageName
import site.addzero.device.protocol.modbus.ksp.core.resolveSpringRouteOutputDir
import site.addzero.device.protocol.modbus.ksp.core.resolveTransportDefaults

/**
 * Modbus TCP 代码生成入口。
 */
class ModbusTcpProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val transport = ModbusTransportKind.TCP
        val enabledTransports = environment.resolveEnabledTransports(defaultTransport = transport)
        if (transport !in enabledTransports) {
            environment.logger.logging(
                "Skip Modbus TCP generation because ${transport.transportId} is not enabled in ${site.addzero.device.protocol.modbus.ksp.core.ModbusKspOptions.TRANSPORTS_OPTION}.",
            )
            return object : SymbolProcessor {
                override fun process(resolver: com.google.devtools.ksp.processing.Resolver): List<KSAnnotated> = emptyList()
            }
        }
        val modes = ModbusCodegenMode.from(environment)
        val externalCArtifactWriter = ModbusExternalCArtifactWriter.from(environment)
        val addressLockFile = ModbusAddressLockFile.from(environment)
        val transportDefaults = environment.resolveTransportDefaults()
        val springRouteOutputDir = environment.resolveSpringRouteOutputDir()
        val contractPackages =
            environment
                .resolveContractPackages()
                .ifEmpty { listOf("site.addzero.device.api.internal") }

        return object : SymbolProcessor {
            private val collected = linkedMapOf<String, CollectedModbusService>()

            override fun process(resolver: com.google.devtools.ksp.processing.Resolver): List<KSAnnotated> {
                ModbusMetadataCollector.collect(
                    environment = environment,
                    resolver = resolver,
                    transport = transport,
                    contractPackages = contractPackages,
                ).forEach { service ->
                    collected[service.model.interfaceQualifiedName] = service
                }
                return emptyList()
            }

            override fun finish() {
                if (collected.isEmpty()) {
                    return
                }

                val services = collected.values.toList()
                val resolvedServices =
                    ModbusAddressPlanner.resolveServices(
                        services = services.map(CollectedModbusService::model),
                        lockFile = addressLockFile,
                        logger = environment.logger,
                    )
                val errors = ModbusModelValidator.validate(resolvedServices)
                if (errors.isNotEmpty()) {
                    errors.forEach(environment.logger::error)
                    return
                }
                ModbusAddressPlanner.persist(resolvedServices, addressLockFile)

                if (ModbusCodegenMode.CONTRACT in modes) {
                    val externalContractSources = mutableListOf<java.io.File>()
                    services.zip(resolvedServices).forEach { (collectedService, resolvedService) ->
                        externalContractSources += writeArtifacts(
                            logger = environment.logger,
                            codeGenerator = environment.codeGenerator,
                            dependencies = dependenciesFor(collectedService.originatingFiles, aggregating = false),
                            artifacts = ModbusArtifactRenderer.renderContractArtifacts(resolvedService, transportDefaults),
                            externalCArtifactWriter = externalCArtifactWriter,
                        )
                    }
                    val kotlinContractServices =
                        services
                            .zip(resolvedServices)
                            .filterNot { (collectedService, _) -> collectedService.providesSourceContract }
                            .map { (_, resolvedService) -> resolvedService }
                    writeArtifacts(
                        logger = environment.logger,
                        codeGenerator = environment.codeGenerator,
                        dependencies = dependenciesFor(services.flatMap(CollectedModbusService::originatingFiles), aggregating = true),
                        artifacts = ModbusArtifactRenderer.renderKotlinContractArtifacts(kotlinContractServices),
                        externalCArtifactWriter = null,
                    )
                    externalContractSources += writeArtifacts(
                        logger = environment.logger,
                        codeGenerator = environment.codeGenerator,
                        dependencies = dependenciesFor(services.flatMap(CollectedModbusService::originatingFiles), aggregating = true),
                        artifacts = ModbusArtifactRenderer.renderTransportContractArtifacts(transport, resolvedServices, transportDefaults),
                        externalCArtifactWriter = externalCArtifactWriter,
                    )
                    ModbusProjectSyncRunner.syncIfNeeded(
                        environment = environment,
                        transport = transport,
                        externalSourceFiles = externalContractSources.filter { file -> file.extension == "c" },
                    )
                }

                if (ModbusCodegenMode.SERVER in modes) {
                    val serverRouteMode =
                        if (springRouteOutputDir == null) {
                            ModbusServerRouteMode.DIRECT_KTOR
                        } else {
                            ModbusServerRouteMode.SPRING_SOURCE
                        }
                    writeArtifacts(
                        logger = environment.logger,
                        codeGenerator = environment.codeGenerator,
                        dependencies = dependenciesFor(services.flatMap(CollectedModbusService::originatingFiles), aggregating = true),
                        artifacts =
                            ModbusArtifactRenderer.renderServerArtifacts(
                                transport = transport,
                                services = resolvedServices,
                                transportDefaults = transportDefaults,
                                serverRouteMode = serverRouteMode,
                            ),
                        externalCArtifactWriter = null,
                    )
                    if (springRouteOutputDir != null) {
                        writeExternalKotlinArtifacts(
                            logger = environment.logger,
                            outputDir = File(springRouteOutputDir),
                            artifacts = ModbusArtifactRenderer.renderSpringRouteSourceArtifacts(transport, resolvedServices),
                        )
                    }
                }

                val apiClientPackageName = environment.resolveApiClientPackageName()
                val apiClientOutputDir = environment.resolveApiClientOutputDir()
                if (apiClientPackageName != null && apiClientOutputDir != null) {
                    writeExternalKotlinArtifacts(
                        logger = environment.logger,
                        outputDir = File(apiClientOutputDir),
                        artifacts = ModbusArtifactRenderer.renderKtorfitClientArtifacts(
                            transport = transport,
                            services = resolvedServices,
                            packageName = apiClientPackageName,
                        ),
                    )
                }
            }
        }
    }

    private fun dependenciesFor(
        files: List<KSFile>,
        aggregating: Boolean,
    ): Dependencies {
        val distinctFiles = files.distinct().toTypedArray()
        return if (distinctFiles.isEmpty()) {
            Dependencies.ALL_FILES
        } else {
            Dependencies(aggregating = aggregating, *distinctFiles)
        }
    }

    private fun writeArtifacts(
        logger: com.google.devtools.ksp.processing.KSPLogger,
        codeGenerator: CodeGenerator,
        dependencies: Dependencies,
        artifacts: List<site.addzero.device.protocol.modbus.ksp.core.GeneratedArtifact>,
        externalCArtifactWriter: ModbusExternalCArtifactWriter?,
    ): List<java.io.File> {
        val externalFiles = mutableListOf<java.io.File>()
        artifacts.forEach { artifact ->
            val output = codeGenerator.createNewFile(
                dependencies = dependencies,
                packageName = artifact.packageName.orEmpty(),
                fileName = artifact.fileName,
                extensionName = artifact.extensionName,
            )
            OutputStreamWriter(output, StandardCharsets.UTF_8).use { writer ->
                writer.write(artifact.content)
            }
            externalCArtifactWriter?.writeIfSupported(artifact, logger)?.let(externalFiles::add)
        }
        return externalFiles
    }

    private fun writeExternalKotlinArtifacts(
        logger: com.google.devtools.ksp.processing.KSPLogger,
        outputDir: File,
        artifacts: List<site.addzero.device.protocol.modbus.ksp.core.GeneratedArtifact>,
    ) {
        artifacts.forEach { artifact ->
            val packageDir =
                artifact.packageName
                    ?.takeIf(String::isNotBlank)
                    ?.replace('.', File.separatorChar)
                    ?: ""
            val targetDir = if (packageDir.isBlank()) outputDir else outputDir.resolve(packageDir)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            val targetFile = targetDir.resolve("${artifact.fileName}.${artifact.extensionName}")
            targetFile.writeText(artifact.content, Charsets.UTF_8)
            logger.info("Generated external Modbus client artifact: ${targetFile.absolutePath}")
        }
    }
}

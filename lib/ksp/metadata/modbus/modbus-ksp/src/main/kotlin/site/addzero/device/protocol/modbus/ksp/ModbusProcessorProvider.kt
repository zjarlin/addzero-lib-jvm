package site.addzero.device.protocol.modbus.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import java.io.File
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import site.addzero.device.protocol.modbus.ksp.core.CollectedModbusService
import site.addzero.device.protocol.modbus.ksp.core.GeneratedArtifact
import site.addzero.device.protocol.modbus.ksp.core.ModbusAddressLockFile
import site.addzero.device.protocol.modbus.ksp.core.ModbusAddressPlanner
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactRenderer
import site.addzero.device.protocol.modbus.ksp.core.ModbusCodegenMode
import site.addzero.device.protocol.modbus.ksp.core.ModbusExternalCArtifactWriter
import site.addzero.device.protocol.modbus.ksp.core.ModbusMetadataCollector
import site.addzero.device.protocol.modbus.ksp.core.ModbusModelValidator
import site.addzero.device.protocol.modbus.ksp.core.ModbusProjectSyncRunner
import site.addzero.device.protocol.modbus.ksp.core.ModbusServerRouteMode
import site.addzero.device.protocol.modbus.ksp.core.ModbusTransportDefaults
import site.addzero.device.protocol.modbus.ksp.core.ModbusTransportKind
import site.addzero.device.protocol.modbus.ksp.core.initializeModbusKspSettings
import site.addzero.device.protocol.modbus.ksp.core.resolveApiClientOutputDir
import site.addzero.device.protocol.modbus.ksp.core.resolveApiClientPackageName
import site.addzero.device.protocol.modbus.ksp.core.resolveContractPackages
import site.addzero.device.protocol.modbus.ksp.core.resolveEnabledTransports
import site.addzero.device.protocol.modbus.ksp.core.resolveSpringRouteOutputDir
import site.addzero.device.protocol.modbus.ksp.core.resolveTransportDefaults
import site.addzero.device.protocol.modbus.ksp.core.ModbusServiceModel

/**
 * Modbus 主处理器入口。
 *
 * 一个处理器统一覆盖 RTU/TCP/MQTT，多 transport 由参数控制，
 * 不再拆成多份几乎完全相同的 provider。
 */
class ModbusProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        initializeModbusKspSettings(environment)
        return object : SymbolProcessor {
            private val enabledTransports = environment.resolveEnabledTransports(defaultTransport = ModbusTransportKind.RTU)
            private val modes = ModbusCodegenMode.from(environment)
            private val externalCArtifactWriter = ModbusExternalCArtifactWriter.from(environment)
            private val addressLockFile = ModbusAddressLockFile.from(environment)
            private val transportDefaults = environment.resolveTransportDefaults()
            private val springRouteOutputDir = environment.resolveSpringRouteOutputDir()
            private val contractPackages =
                environment
                    .resolveContractPackages()
                    .ifEmpty { listOf("site.addzero.device.api.internal") }
            private val collected =
                linkedMapOf<ModbusTransportKind, LinkedHashMap<String, CollectedModbusService>>()

            override fun process(resolver: Resolver): List<KSAnnotated> {
                enabledTransports.forEach { transport ->
                    val transportBucket = collected.getOrPut(transport, ::linkedMapOf)
                    ModbusMetadataCollector.collect(
                        environment = environment,
                        resolver = resolver,
                        transport = transport,
                        contractPackages = contractPackages,
                    ).forEach { service ->
                        transportBucket[service.model.interfaceQualifiedName] = service
                    }
                }
                return emptyList()
            }

            override fun finish() {
                collected.forEach { (transport, servicesByInterface) ->
                    val services = servicesByInterface.values.toList()
                    if (services.isEmpty()) {
                        return@forEach
                    }
                    generateTransportArtifacts(
                        environment = environment,
                        transport = transport,
                        services = services,
                        modes = modes,
                        externalCArtifactWriter = externalCArtifactWriter,
                        addressLockFile = addressLockFile,
                        transportDefaults = transportDefaults,
                        springRouteOutputDir = springRouteOutputDir,
                    )
                }
            }
        }
    }

    private fun generateTransportArtifacts(
        environment: SymbolProcessorEnvironment,
        transport: ModbusTransportKind,
        services: List<CollectedModbusService>,
        modes: Set<ModbusCodegenMode>,
        externalCArtifactWriter: ModbusExternalCArtifactWriter?,
        addressLockFile: ModbusAddressLockFile?,
        transportDefaults: ModbusTransportDefaults,
        springRouteOutputDir: String?,
    ) {
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
            generateContractArtifacts(
                environment = environment,
                transport = transport,
                services = services,
                resolvedServices = resolvedServices,
                transportDefaults = transportDefaults,
                externalCArtifactWriter = externalCArtifactWriter,
            )
        }

        if (ModbusCodegenMode.SERVER in modes) {
            generateServerArtifacts(
                environment = environment,
                transport = transport,
                services = services,
                resolvedServices = resolvedServices,
                transportDefaults = transportDefaults,
                springRouteOutputDir = springRouteOutputDir,
            )
        }

        val apiClientPackageName = environment.resolveApiClientPackageName()
        val apiClientOutputDir = environment.resolveApiClientOutputDir()
        if (apiClientPackageName != null && apiClientOutputDir != null) {
            writeExternalKotlinArtifacts(
                logger = environment.logger,
                outputDir = File(apiClientOutputDir),
                artifacts =
                    ModbusArtifactRenderer.renderKtorfitClientArtifacts(
                        transport = transport,
                        services = resolvedServices,
                        packageName = apiClientPackageName,
                    ),
            )
        }
    }

    private fun generateContractArtifacts(
        environment: SymbolProcessorEnvironment,
        transport: ModbusTransportKind,
        services: List<CollectedModbusService>,
        resolvedServices: List<ModbusServiceModel>,
        transportDefaults: ModbusTransportDefaults,
        externalCArtifactWriter: ModbusExternalCArtifactWriter?,
    ) {
        val externalContractSources = mutableListOf<File>()
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
            artifacts =
                ModbusArtifactRenderer.renderTransportContractArtifacts(
                    transport = transport,
                    services = resolvedServices,
                    transportDefaults = transportDefaults,
                ),
            externalCArtifactWriter = externalCArtifactWriter,
        )
        ModbusProjectSyncRunner.syncIfNeeded(
            environment = environment,
            transport = transport,
            externalSourceFiles = externalContractSources.filter { file -> file.extension == "c" },
        )
    }

    private fun generateServerArtifacts(
        environment: SymbolProcessorEnvironment,
        transport: ModbusTransportKind,
        services: List<CollectedModbusService>,
        resolvedServices: List<ModbusServiceModel>,
        transportDefaults: ModbusTransportDefaults,
        springRouteOutputDir: String?,
    ) {
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
        logger: KSPLogger,
        codeGenerator: CodeGenerator,
        dependencies: Dependencies,
        artifacts: List<GeneratedArtifact>,
        externalCArtifactWriter: ModbusExternalCArtifactWriter?,
    ): List<File> {
        val externalFiles = mutableListOf<File>()
        artifacts.forEach { artifact ->
            val output =
                codeGenerator.createNewFile(
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
        logger: KSPLogger,
        outputDir: File,
        artifacts: List<GeneratedArtifact>,
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

package site.addzero.device.protocol.modbus.ksp.rtu

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import site.addzero.device.protocol.modbus.ksp.core.CollectedModbusService
import site.addzero.device.protocol.modbus.ksp.core.ModbusAddressLockFile
import site.addzero.device.protocol.modbus.ksp.core.ModbusAddressPlanner
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactRenderer
import site.addzero.device.protocol.modbus.ksp.core.ModbusCodegenMode
import site.addzero.device.protocol.modbus.ksp.core.ModbusExternalCArtifactWriter
import site.addzero.device.protocol.modbus.ksp.core.ModbusModelValidator
import site.addzero.device.protocol.modbus.ksp.core.ModbusProjectSyncRunner
import site.addzero.device.protocol.modbus.ksp.core.ModbusSymbolCollector
import site.addzero.device.protocol.modbus.ksp.core.ModbusTransportKind

/**
 * Modbus RTU 代码生成入口。
 */
class ModbusRtuProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val collector = ModbusSymbolCollector(environment.logger)
        val modes = ModbusCodegenMode.from(environment)
        val externalCArtifactWriter = ModbusExternalCArtifactWriter.from(environment)
        val addressLockFile = ModbusAddressLockFile.from(environment)
        val contractPackages =
            environment
                .resolveContractPackages()
                .ifEmpty { listOf("site.addzero.device.api.internal") }

        return object : SymbolProcessor {
            private val collected = linkedMapOf<String, CollectedModbusService>()

            override fun process(resolver: com.google.devtools.ksp.processing.Resolver): List<KSAnnotated> {
                collector.collect(
                    resolver = resolver,
                    transport = ModbusTransportKind.RTU,
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
                            artifacts = ModbusArtifactRenderer.renderContractArtifacts(resolvedService),
                            externalCArtifactWriter = externalCArtifactWriter,
                        )
                    }
                    externalContractSources += writeArtifacts(
                        logger = environment.logger,
                        codeGenerator = environment.codeGenerator,
                        dependencies = dependenciesFor(services.flatMap(CollectedModbusService::originatingFiles), aggregating = true),
                        artifacts = ModbusArtifactRenderer.renderTransportContractArtifacts(ModbusTransportKind.RTU, resolvedServices),
                        externalCArtifactWriter = externalCArtifactWriter,
                    )
                    ModbusProjectSyncRunner.syncIfNeeded(
                        environment = environment,
                        transport = ModbusTransportKind.RTU,
                        externalSourceFiles = externalContractSources.filter { file -> file.extension == "c" },
                    )
                }

                if (ModbusCodegenMode.SERVER in modes) {
                    writeArtifacts(
                        logger = environment.logger,
                        codeGenerator = environment.codeGenerator,
                        dependencies = dependenciesFor(services.flatMap(CollectedModbusService::originatingFiles), aggregating = true),
                        artifacts = ModbusArtifactRenderer.renderServerArtifacts(ModbusTransportKind.RTU, resolvedServices),
                        externalCArtifactWriter = null,
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

    private fun SymbolProcessorEnvironment.resolveContractPackages(): List<String> =
        listOfNotNull(
            options["addzero.modbus.contractPackages"],
            options["addzero.modbus.contractPackage"],
        ).flatMap { rawValue ->
            rawValue
                .split(',', ';', '\n')
                .map(String::trim)
                .filter(String::isNotBlank)
        }.distinct()
}

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
import site.addzero.device.protocol.modbus.ksp.core.CollectedModbusService
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactRenderer
import site.addzero.device.protocol.modbus.ksp.core.ModbusCodegenMode
import site.addzero.device.protocol.modbus.ksp.core.ModbusModelValidator
import site.addzero.device.protocol.modbus.ksp.core.ModbusSymbolCollector
import site.addzero.device.protocol.modbus.ksp.core.ModbusTransportKind

/**
 * Modbus TCP 代码生成入口。
 */
class ModbusTcpProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val collector = ModbusSymbolCollector(environment.logger)
        val modes = ModbusCodegenMode.from(environment)
        val contractPackages =
            environment
                .resolveContractPackages()
                .ifEmpty { listOf("site.addzero.device.api.internal") }

        return object : SymbolProcessor {
            private val collected = linkedMapOf<String, CollectedModbusService>()

            override fun process(resolver: com.google.devtools.ksp.processing.Resolver): List<KSAnnotated> {
                collector.collect(
                    resolver = resolver,
                    transport = ModbusTransportKind.TCP,
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
                val errors = ModbusModelValidator.validate(services.map(CollectedModbusService::model))
                if (errors.isNotEmpty()) {
                    errors.forEach(environment.logger::error)
                    return
                }

                if (ModbusCodegenMode.CONTRACT in modes) {
                    services.forEach { service ->
                        writeArtifacts(
                            codeGenerator = environment.codeGenerator,
                            dependencies = dependenciesFor(service.originatingFiles, aggregating = false),
                            artifacts = ModbusArtifactRenderer.renderContractArtifacts(service.model),
                        )
                    }
                    writeArtifacts(
                        codeGenerator = environment.codeGenerator,
                        dependencies = dependenciesFor(services.flatMap(CollectedModbusService::originatingFiles), aggregating = true),
                        artifacts = ModbusArtifactRenderer.renderTransportContractArtifacts(ModbusTransportKind.TCP, services.map(CollectedModbusService::model)),
                    )
                }

                if (ModbusCodegenMode.SERVER in modes) {
                    writeArtifacts(
                        codeGenerator = environment.codeGenerator,
                        dependencies = dependenciesFor(services.flatMap(CollectedModbusService::originatingFiles), aggregating = true),
                        artifacts = ModbusArtifactRenderer.renderServerArtifacts(ModbusTransportKind.TCP, services.map(CollectedModbusService::model)),
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
        codeGenerator: CodeGenerator,
        dependencies: Dependencies,
        artifacts: List<site.addzero.device.protocol.modbus.ksp.core.GeneratedArtifact>,
    ) {
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
        }
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

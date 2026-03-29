package site.addzero.ksp.modbusrtu.gradle

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedCompanionDependency
import site.addzero.gradle.kspconsumer.PublishedDependencyScope
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

abstract class ModbusRtuExtension {
    abstract val codegenModes: ListProperty<String>
    abstract val contractPackages: ListProperty<String>

    init {
        codegenModes.convention(listOf("server"))
        contractPackages.convention(emptyList())
    }
}

class ModbusRtuGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.JVM,
            localProjectPath = ":lib:ksp:metadata:modbus:modbus-ksp-rtu",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )
    override val companionDependencies: List<PublishedCompanionDependency> =
        listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.JVM,
                localProjectPath = ":lib:ksp:metadata:modbus:modbus-runtime",
                artifactId = "modbus-runtime",
            ),
        )

    override fun createExtension(project: Project): Any {
        return createTypedExtension(project, EXTENSION_NAME, ModbusRtuExtension::class.java)
    }

    override fun collectKspArgs(project: Project, extension: Any?): Map<String, String> {
        val modbus = extension as ModbusRtuExtension
        return linkedMapOf<String, String>().apply {
            put("addzero.modbus.codegen.mode", modbus.codegenModes.get().joinToString(","))
            modbus.contractPackages.get()
                .takeIf { it.isNotEmpty() }
                ?.let { put("addzero.modbus.contractPackages", it.joinToString(",")) }
        }
    }

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.modbus-rtu"
        const val EXTENSION_NAME: String = "modbusRtu"
        const val PROCESSOR_ARTIFACT_ID: String = "modbus-ksp-rtu"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/modbus-rtu/gradle-plugin.properties"
    }
}

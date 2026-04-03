package site.addzero.ksp.enumprocessor.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

abstract class EnumProcessorExtension {
    abstract val enumOutputPackage: Property<String>

    init {
        enumOutputPackage.convention("site.addzero.generated.enum")
    }
}

class EnumGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:enum-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )

    override fun createExtension(project: Project): Any {
        return createTypedExtension(project, EXTENSION_NAME, EnumProcessorExtension::class.java)
    }

    override fun collectKspArgs(project: Project, extension: Any?): Map<String, String> {
        val enumProcessor = extension as EnumProcessorExtension
        return linkedMapOf(
            "enumOutputPackage" to enumProcessor.enumOutputPackage.get(),
        )
    }

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.enum"
        const val EXTENSION_NAME: String = "enumProcessor"
        const val PROCESSOR_ARTIFACT_ID: String = "enum-processor"
        const val COORDINATES_RESOURCE_PATH: String = "site/addzero/ksp/enum/gradle-plugin.properties"
    }
}

package site.addzero.ksp.enumprocessor.gradle

import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

class EnumGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:enum-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.enum"
        const val PROCESSOR_ARTIFACT_ID: String = "enum-processor"
        const val COORDINATES_RESOURCE_PATH: String = "site/addzero/ksp/enum/gradle-plugin.properties"
    }
}

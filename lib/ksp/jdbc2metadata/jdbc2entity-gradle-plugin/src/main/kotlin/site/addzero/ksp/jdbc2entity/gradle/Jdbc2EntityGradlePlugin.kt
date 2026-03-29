package site.addzero.ksp.jdbc2entity.gradle

import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

class Jdbc2EntityGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:jdbc2metadata:jdbc2entity-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.jdbc2entity"
        const val PROCESSOR_ARTIFACT_ID: String = "jdbc2entity-processor"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/jdbc2entity/gradle-plugin.properties"
    }
}

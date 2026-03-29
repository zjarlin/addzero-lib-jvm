package site.addzero.ksp.apiprovider.gradle

import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

class ApiProviderGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:apiprovider-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.apiprovider"
        const val PROCESSOR_ARTIFACT_ID: String = "apiprovider-processor"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/apiprovider/gradle-plugin.properties"
    }
}

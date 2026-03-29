package site.addzero.ksp.genreified.gradle

import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

class GenReifiedGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:gen-reified:gen-reified-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.gen-reified"
        const val PROCESSOR_ARTIFACT_ID: String = "gen-reified-processor"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/gen-reified/gradle-plugin.properties"
    }
}

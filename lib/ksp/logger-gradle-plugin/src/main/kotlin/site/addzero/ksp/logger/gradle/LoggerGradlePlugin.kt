package site.addzero.ksp.logger.gradle

import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

class LoggerGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.JVM,
            localProjectPath = ":lib:ksp:logger-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.logger"
        const val PROCESSOR_ARTIFACT_ID: String = "logger-processor"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/logger/gradle-plugin.properties"
    }
}

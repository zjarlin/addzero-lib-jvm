package site.addzero.ksp.jdbc2controller.gradle

import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

class Jdbc2ControllerGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:jdbc2metadata:jdbc2controller-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.jdbc2controller"
        const val PROCESSOR_ARTIFACT_ID: String = "jdbc2controller-processor"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/jdbc2controller/gradle-plugin.properties"
    }
}

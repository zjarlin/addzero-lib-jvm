package site.addzero.ksp.multireceiver.gradle

import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedCompanionDependency
import site.addzero.gradle.kspconsumer.PublishedDependencyScope
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

class MultireceiverGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.JVM,
            localProjectPath = ":lib:ksp:metadata:multireceiver-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )
    override val companionDependencies: List<PublishedCompanionDependency> =
        listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.KMP,
                localProjectPath = ":lib:kcp:multireceiver:kcp-multireceiver-annotations",
                artifactId = "kcp-multireceiver-annotations",
            ),
        )

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.multireceiver"
        const val PROCESSOR_ARTIFACT_ID: String = "multireceiver-processor"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/multireceiver/gradle-plugin.properties"
    }
}

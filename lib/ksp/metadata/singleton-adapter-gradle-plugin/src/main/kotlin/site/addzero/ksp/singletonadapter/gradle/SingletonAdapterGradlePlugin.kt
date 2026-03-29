package site.addzero.ksp.singletonadapter.gradle

import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedCompanionDependency
import site.addzero.gradle.kspconsumer.PublishedDependencyScope
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

class SingletonAdapterGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.JVM,
            localProjectPath = ":lib:ksp:metadata:singleton-adapter-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )
    override val companionDependencies: List<PublishedCompanionDependency> =
        listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.JVM,
                localProjectPath = ":lib:ksp:metadata:singleton-adapter-api",
                artifactId = "singleton-adapter-api",
            ),
        )

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.singleton-adapter"
        const val PROCESSOR_ARTIFACT_ID: String = "singleton-adapter-processor"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/singleton-adapter/gradle-plugin.properties"
    }
}

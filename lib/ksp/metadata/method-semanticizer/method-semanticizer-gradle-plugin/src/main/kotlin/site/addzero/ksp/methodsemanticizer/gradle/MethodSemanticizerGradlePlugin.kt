package site.addzero.ksp.methodsemanticizer.gradle

import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedCompanionDependency
import site.addzero.gradle.kspconsumer.PublishedDependencyScope
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

class MethodSemanticizerGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:method-semanticizer:method-semanticizer-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )
    override val companionDependencies: List<PublishedCompanionDependency> =
        listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.KMP,
                localProjectPath = ":lib:ksp:metadata:method-semanticizer:method-semanticizer-api",
                artifactId = "method-semanticizer-api",
            ),
        )

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.method-semanticizer"
        const val PROCESSOR_ARTIFACT_ID: String = "method-semanticizer-processor"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/method-semanticizer/gradle-plugin.properties"
    }
}

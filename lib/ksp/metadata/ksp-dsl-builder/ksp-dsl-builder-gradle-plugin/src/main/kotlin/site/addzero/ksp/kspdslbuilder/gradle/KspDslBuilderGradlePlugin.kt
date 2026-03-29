package site.addzero.ksp.kspdslbuilder.gradle

import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

class KspDslBuilderGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.ksp-dsl-builder"
        const val PROCESSOR_ARTIFACT_ID: String = "ksp-dsl-builder-processor"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/ksp-dsl-builder/gradle-plugin.properties"
    }
}

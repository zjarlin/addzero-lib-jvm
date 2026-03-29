package site.addzero.ksp.composeprops.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedCompanionDependency
import site.addzero.gradle.kspconsumer.PublishedDependencyScope
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

abstract class ComposePropsExtension {
    abstract val suffix: Property<String>

    init {
        suffix.convention("State")
    }
}

class ComposePropsGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:compose-props:compose-props-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )
    override val companionDependencies: List<PublishedCompanionDependency> =
        listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.KMP,
                localProjectPath = ":lib:ksp:metadata:compose-props:compose-props-annotations",
                artifactId = "compose-props-annotations",
            ),
        )

    override fun createExtension(project: Project): Any {
        return createTypedExtension(project, EXTENSION_NAME, ComposePropsExtension::class.java)
    }

    override fun collectKspArgs(project: Project, extension: Any?): Map<String, String> {
        val composeProps = extension as ComposePropsExtension
        return mapOf("COMPOSE_ATTRS_SUFFIX" to composeProps.suffix.get())
    }

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.compose-props"
        const val EXTENSION_NAME: String = "composeProps"
        const val PROCESSOR_ARTIFACT_ID: String = "compose-props-processor"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/compose-props/gradle-plugin.properties"
    }
}

package site.addzero.ksp.spring2ktorserver.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedCompanionDependency
import site.addzero.gradle.kspconsumer.PublishedDependencyScope
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

abstract class Spring2KtorServerExtension {
    abstract val generatedPackage: Property<String>

    init {
        generatedPackage.convention("")
    }
}

class Spring2KtorServerGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.JVM,
            localProjectPath = ":lib:ksp:metadata:spring2ktor-server-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )
    override val companionDependencies: List<PublishedCompanionDependency> =
        listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.JVM,
                localProjectPath = ":lib:ksp:metadata:spring2ktor-server-core",
                artifactId = "spring2ktor-server-core",
            ),
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.COMPILE_ONLY,
                artifactKind = PublishedKspArtifactKind.JVM,
                notation = "org.springframework:spring-web:5.3.21",
            ),
        )

    override fun createExtension(project: Project): Any {
        return createTypedExtension(project, EXTENSION_NAME, Spring2KtorServerExtension::class.java)
    }

    override fun collectKspArgs(project: Project, extension: Any?): Map<String, String> {
        val spring2Ktor = extension as Spring2KtorServerExtension
        return spring2Ktor.generatedPackage.orNull
            ?.takeIf(String::isNotBlank)
            ?.let { mapOf("springKtor.generatedPackage" to it) }
            ?: emptyMap()
    }

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.spring2ktor-server"
        const val EXTENSION_NAME: String = "spring2ktorServer"
        const val PROCESSOR_ARTIFACT_ID: String = "spring2ktor-server-processor"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/spring2ktor-server/gradle-plugin.properties"
    }
}

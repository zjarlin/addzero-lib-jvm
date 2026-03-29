package site.addzero.ksp.ioc.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedCompanionDependency
import site.addzero.gradle.kspconsumer.PublishedDependencyScope
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

abstract class IocExtension {
    abstract val modulePackage: Property<String>
    abstract val app: Property<Boolean>

    init {
        modulePackage.convention("")
        app.convention(false)
    }
}

class IocGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:ioc:ioc-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )
    override val companionDependencies: List<PublishedCompanionDependency> =
        listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.KMP,
                localProjectPath = ":lib:ksp:metadata:ioc:ioc-core",
                artifactId = "ioc-core",
            ),
        )

    override fun createExtension(project: Project): Any {
        return createTypedExtension(project, EXTENSION_NAME, IocExtension::class.java)
    }

    override fun collectKspArgs(project: Project, extension: Any?): Map<String, String> {
        val ioc = extension as IocExtension
        return linkedMapOf<String, String>().apply {
            put("ioc.role", if (ioc.app.get()) "app" else "lib")
            ioc.modulePackage.orNull
                ?.takeIf(String::isNotBlank)
                ?.let { put("ioc.module", it) }
        }
    }

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.ioc"
        const val EXTENSION_NAME: String = "ioc"
        const val PROCESSOR_ARTIFACT_ID: String = "ioc-processor"
        const val COORDINATES_RESOURCE_PATH: String = "site/addzero/ksp/ioc/gradle-plugin.properties"
    }
}

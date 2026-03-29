package site.addzero.ksp.controller2api.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

abstract class Controller2ApiExtension {
    abstract val generatedPackage: Property<String>
    abstract val outputDir: Property<String>

    init {
        generatedPackage.convention("site.addzero.generated.api")
        outputDir.convention("")
    }
}

class Controller2ApiGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:controller2api-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )

    override fun createExtension(project: Project): Any {
        return createTypedExtension(project, EXTENSION_NAME, Controller2ApiExtension::class.java)
    }

    override fun collectKspArgs(project: Project, extension: Any?): Map<String, String> {
        val controller2Api = extension as Controller2ApiExtension
        val packageName = controller2Api.generatedPackage.get()
        val outputDir = controller2Api.outputDir.orNull
            ?.takeIf(String::isNotBlank)
            ?: packageDirectory(defaultSourceDirectory(project), packageName)
        return mapOf(
            "apiClientPackageName" to packageName,
            "apiClientOutputDir" to outputDir,
        )
    }

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.controller2api"
        const val EXTENSION_NAME: String = "controller2api"
        const val PROCESSOR_ARTIFACT_ID: String = "controller2api-processor"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/controller2api/gradle-plugin.properties"
    }
}

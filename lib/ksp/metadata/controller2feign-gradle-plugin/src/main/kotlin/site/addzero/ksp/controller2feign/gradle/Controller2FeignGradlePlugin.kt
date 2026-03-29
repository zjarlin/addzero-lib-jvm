package site.addzero.ksp.controller2feign.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

abstract class Controller2FeignExtension {
    abstract val outputPackage: Property<String>
    abstract val outputDir: Property<String>
    abstract val enabled: Property<Boolean>

    init {
        outputPackage.convention("site.addzero.generated.feign")
        outputDir.convention("")
        enabled.convention(true)
    }
}

class Controller2FeignGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:controller2feign-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )

    override fun createExtension(project: Project): Any {
        return createTypedExtension(project, EXTENSION_NAME, Controller2FeignExtension::class.java)
    }

    override fun collectKspArgs(project: Project, extension: Any?): Map<String, String> {
        val controller2Feign = extension as Controller2FeignExtension
        return linkedMapOf<String, String>().apply {
            put("feignOutputPackage", controller2Feign.outputPackage.get())
            put("feignEnabled", controller2Feign.enabled.get().toString())
            controller2Feign.outputDir.orNull
                ?.takeIf(String::isNotBlank)
                ?.let { put("feignOutputDir", it) }
        }
    }

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.controller2feign"
        const val EXTENSION_NAME: String = "controller2feign"
        const val PROCESSOR_ARTIFACT_ID: String = "controller2feign-processor"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/controller2feign/gradle-plugin.properties"
    }
}

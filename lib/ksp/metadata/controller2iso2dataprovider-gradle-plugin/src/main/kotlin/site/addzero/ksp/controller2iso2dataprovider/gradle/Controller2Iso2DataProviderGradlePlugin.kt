package site.addzero.ksp.controller2iso2dataprovider.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

abstract class Controller2Iso2DataProviderExtension {
    abstract val sharedComposeSourceDir: Property<String>
    abstract val generatedPackage: Property<String>
    abstract val apiClientPackageName: Property<String>
    abstract val isomorphicPackageName: Property<String>

    init {
        sharedComposeSourceDir.convention("")
        generatedPackage.convention("site.addzero.generated.forms.dataprovider")
        apiClientPackageName.convention("site.addzero.generated.api")
        isomorphicPackageName.convention("site.addzero.generated.isomorphic")
    }
}

class Controller2Iso2DataProviderGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:controller2iso2dataprovider-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )

    override fun createExtension(project: Project): Any {
        return createTypedExtension(project, EXTENSION_NAME, Controller2Iso2DataProviderExtension::class.java)
    }

    override fun collectKspArgs(project: Project, extension: Any?): Map<String, String> {
        val iso2DataProvider = extension as Controller2Iso2DataProviderExtension
        return mapOf(
            "sharedComposeSourceDir" to (
                iso2DataProvider.sharedComposeSourceDir.orNull
                    ?.takeIf(String::isNotBlank)
                    ?: defaultSourceDirectory(project)
                ),
            "iso2DataProviderPackage" to iso2DataProvider.generatedPackage.get(),
            "apiClientPackageName" to iso2DataProvider.apiClientPackageName.get(),
            "isomorphicPackageName" to iso2DataProvider.isomorphicPackageName.get(),
        )
    }

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.controller2iso2dataprovider"
        const val EXTENSION_NAME: String = "controller2iso2dataprovider"
        const val PROCESSOR_ARTIFACT_ID: String = "controller2iso2dataprovider-processor"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/controller2iso2dataprovider/gradle-plugin.properties"
    }
}

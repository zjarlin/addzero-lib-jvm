package site.addzero.ksp.jimmerentityexternal.gradle

import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

abstract class JimmerEntity2IsoExtension {
    abstract val packageName: Property<String>
    abstract val classSuffix: Property<String>

    init {
        packageName.convention("site.addzero.generated.isomorphic")
        classSuffix.convention("Iso")
    }
}

abstract class JimmerEntity2FormExtension {
    abstract val packageName: Property<String>

    init {
        packageName.convention("site.addzero.generated.forms")
    }
}

abstract class JimmerEntity2McpExtension {
    abstract val packageName: Property<String>

    init {
        packageName.convention("site.addzero.generated.mcp")
    }
}

abstract class JimmerEntityExternalExtension @Inject constructor(
    objects: ObjectFactory,
) {
    abstract val sharedSourceDir: Property<String>
    abstract val sharedComposeSourceDir: Property<String>
    abstract val backendServerSourceDir: Property<String>
    abstract val apiClientPackageName: Property<String>
    abstract val enumOutputPackage: Property<String>
    abstract val iso2DataProviderPackage: Property<String>

    val entity2Iso: JimmerEntity2IsoExtension = objects.newInstance(JimmerEntity2IsoExtension::class.java)
    val entity2Form: JimmerEntity2FormExtension = objects.newInstance(JimmerEntity2FormExtension::class.java)
    val entity2Mcp: JimmerEntity2McpExtension = objects.newInstance(JimmerEntity2McpExtension::class.java)

    init {
        sharedSourceDir.convention("")
        sharedComposeSourceDir.convention("")
        backendServerSourceDir.convention("")
        apiClientPackageName.convention("site.addzero.generated.api")
        enumOutputPackage.convention("site.addzero.generated.enums")
        iso2DataProviderPackage.convention("site.addzero.generated.forms.dataprovider")
    }
}

class JimmerEntityExternalGradlePlugin : AbstractPublishedKspConsumerPlugin() {
    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:jimmer-entity-external-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )
    override val additionalProcessorArtifacts: List<PublishedProcessorArtifact> =
        listOf(
            PublishedProcessorArtifact(
                artifactKind = PublishedKspArtifactKind.KMP,
                localProjectPath = ":lib:ksp:metadata:entity2iso-processor",
                artifactId = "entity2iso-processor",
            ),
            PublishedProcessorArtifact(
                artifactKind = PublishedKspArtifactKind.KMP,
                localProjectPath = ":lib:ksp:metadata:entity2form:entity2form-processor",
                artifactId = "entity2form-processor",
            ),
            PublishedProcessorArtifact(
                artifactKind = PublishedKspArtifactKind.KMP,
                localProjectPath = ":lib:ksp:metadata:entity2mcp-processor",
                artifactId = "entity2mcp-processor",
            ),
        )

    override fun createExtension(project: Project): Any {
        return createTypedExtension(
            project,
            EXTENSION_NAME,
            JimmerEntityExternalExtension::class.java,
            project.objects,
        )
    }

    override fun collectKspArgs(project: Project, extension: Any?): Map<String, String> {
        val jimmer = extension as JimmerEntityExternalExtension
        val sharedSourceDir = jimmer.sharedSourceDir.orNull
            ?.takeIf(String::isNotBlank)
            ?: defaultSourceDirectory(project)
        val sharedComposeSourceDir = jimmer.sharedComposeSourceDir.orNull
            ?.takeIf(String::isNotBlank)
            ?: defaultSourceDirectory(project)
        val backendServerSourceDir = jimmer.backendServerSourceDir.orNull
            ?.takeIf(String::isNotBlank)
            ?: defaultSourceDirectory(project)
        val isoPackage = jimmer.entity2Iso.packageName.get()
        return linkedMapOf(
            "isomorphicPkg" to isoPackage,
            "isomorphicGenDir" to packageDirectory(sharedSourceDir, isoPackage),
            "sharedSourceDir" to sharedSourceDir,
            "sharedComposeSourceDir" to sharedComposeSourceDir,
            "backendServerSourceDir" to backendServerSourceDir,
            "isomorphicPackageName" to isoPackage,
            "isomorphicClassSuffix" to jimmer.entity2Iso.classSuffix.get(),
            "formPackageName" to jimmer.entity2Form.packageName.get(),
            "enumOutputPackage" to jimmer.enumOutputPackage.get(),
            "apiClientPackageName" to jimmer.apiClientPackageName.get(),
            "iso2DataProviderPackage" to jimmer.iso2DataProviderPackage.get(),
            "mcpPackageName" to jimmer.entity2Mcp.packageName.get(),
        )
    }

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.jimmer-entity-external"
        const val EXTENSION_NAME: String = "jimmerEntityExternal"
        const val PROCESSOR_ARTIFACT_ID: String = "jimmer-entity-external-processor"
        const val COORDINATES_RESOURCE_PATH: String =
            "site/addzero/ksp/jimmer-entity-external/gradle-plugin.properties"
    }
}

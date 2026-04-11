package site.addzero.ksp

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedKspConsumerDefinition
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact
import site.addzero.gradle.kspconsumer.configurePublishedKspConsumer
import site.addzero.gradle.kspconsumer.defaultPublishedKspSourceDirectory
import site.addzero.gradle.kspconsumer.publishedKspPackageDirectory

abstract class JimmerEntity2IsoExtension {
    abstract val enabled: Property<Boolean>
    abstract val packageName: Property<String>
    abstract val classSuffix: Property<String>
    abstract val serializableEnabled: Property<Boolean>

    init {
        enabled.convention(true)
        packageName.convention("site.addzero.generated.isomorphic")
        classSuffix.convention("Iso")
        serializableEnabled.convention(true)
    }
}

abstract class JimmerEntity2FormExtension {
    abstract val enabled: Property<Boolean>
    abstract val packageName: Property<String>

    init {
        enabled.convention(true)
        packageName.convention("site.addzero.generated.forms")
    }
}

abstract class JimmerEntity2McpExtension {
    abstract val enabled: Property<Boolean>
    abstract val packageName: Property<String>

    init {
        enabled.convention(true)
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

val publishedKspResourceClassLoader = Thread.currentThread().contextClassLoader ?: javaClass.classLoader
val jimmerEntityExternal = extensions.create<JimmerEntityExternalExtension>(
    "jimmerEntityExternal",
    objects,
)

configurePublishedKspConsumer(
    definition = PublishedKspConsumerDefinition(
        pluginId = "site.addzero.ksp.jimmer-entity-external",
        coordinatesResourcePath = "site/addzero/ksp/jimmer-entity-external/gradle-plugin.properties",
        resourceClassLoader = publishedKspResourceClassLoader,
        processorArtifact = PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:jimmer-entity-external-processor",
            artifactId = "jimmer-entity-external-processor",
        ),
        additionalProcessorArtifacts = listOf(
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
            PublishedProcessorArtifact(
                artifactKind = PublishedKspArtifactKind.KMP,
                localProjectPath = ":lib:ksp:metadata:controller2iso2dataprovider-processor",
                artifactId = "controller2iso2dataprovider-processor",
            ),
        ),
    ),
) {
    val sharedSourceDir = jimmerEntityExternal.sharedSourceDir.orNull
        ?.takeIf(String::isNotBlank)
        ?: defaultPublishedKspSourceDirectory()
    val sharedComposeSourceDir = jimmerEntityExternal.sharedComposeSourceDir.orNull
        ?.takeIf(String::isNotBlank)
        ?: defaultPublishedKspSourceDirectory()
    val backendServerSourceDir = jimmerEntityExternal.backendServerSourceDir.orNull
        ?.takeIf(String::isNotBlank)
        ?: defaultPublishedKspSourceDirectory()
    val isoPackage = jimmerEntityExternal.entity2Iso.packageName.get()
    linkedMapOf(
        "isomorphicPkg" to isoPackage,
        "isomorphicGenDir" to publishedKspPackageDirectory(sharedSourceDir, isoPackage),
        "sharedSourceDir" to sharedSourceDir,
        "sharedComposeSourceDir" to sharedComposeSourceDir,
        "backendServerSourceDir" to backendServerSourceDir,
        "isomorphicPackageName" to isoPackage,
        "isomorphicClassSuffix" to jimmerEntityExternal.entity2Iso.classSuffix.get(),
        "isomorphicSerializableEnabled" to jimmerEntityExternal.entity2Iso.serializableEnabled.get().toString(),
        "entity2Iso.enabled" to jimmerEntityExternal.entity2Iso.enabled.get().toString(),
        "entity2Form.enabled" to jimmerEntityExternal.entity2Form.enabled.get().toString(),
        "entity2Mcp.enabled" to jimmerEntityExternal.entity2Mcp.enabled.get().toString(),
        "formPackageName" to jimmerEntityExternal.entity2Form.packageName.get(),
        "enumOutputPackage" to jimmerEntityExternal.enumOutputPackage.get(),
        "apiClientPackageName" to jimmerEntityExternal.apiClientPackageName.get(),
        "iso2DataProviderPackage" to jimmerEntityExternal.iso2DataProviderPackage.get(),
        "mcpPackageName" to jimmerEntityExternal.entity2Mcp.packageName.get(),
    )
}

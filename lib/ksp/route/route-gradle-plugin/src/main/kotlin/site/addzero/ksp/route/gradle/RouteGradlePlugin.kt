package site.addzero.ksp.route.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import site.addzero.gradle.kspconsumer.AbstractPublishedKspConsumerPlugin
import site.addzero.gradle.kspconsumer.PublishedCompanionDependency
import site.addzero.gradle.kspconsumer.PublishedDependencyScope
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact

abstract class RouteExtension {
    abstract val sharedSourceDir: Property<String>
    abstract val generatedPackage: Property<String>
    abstract val routeOwnerModule: Property<String>
    abstract val moduleKey: Property<String>

    init {
        sharedSourceDir.convention("")
        generatedPackage.convention("site.addzero.generated")
        routeOwnerModule.convention("")
        moduleKey.convention("")
    }
}

class RouteGradlePlugin : AbstractPublishedKspConsumerPlugin() {

    override val pluginId: String = PLUGIN_ID
    override val coordinatesResourcePath: String = COORDINATES_RESOURCE_PATH
    override val processorArtifact: PublishedProcessorArtifact =
        PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:route:route-processor",
            artifactId = PROCESSOR_ARTIFACT_ID,
        )
    override val companionDependencies: List<PublishedCompanionDependency> =
        listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.KMP,
                localProjectPath = ":lib:ksp:route:route-core",
                artifactId = "route-core",
            ),
        )

    override fun createExtension(project: Project): Any {
        return createTypedExtension(project, EXTENSION_NAME, RouteExtension::class.java)
    }

    override fun collectKspArgs(project: Project, extension: Any?): Map<String, String> {
        val route = extension as RouteExtension
        val sharedSourceDir = route.sharedSourceDir.orNull
            ?.takeIf(String::isNotBlank)
            ?: defaultSourceDirectory(project)
        val routeOwnerModule = route.routeOwnerModule.orNull
            ?.takeIf(String::isNotBlank)
            ?: defaultSourceDirectory(project)
        return linkedMapOf<String, String>().apply {
            put("sharedSourceDir", sharedSourceDir)
            put("routeGenPkg", route.generatedPackage.get())
            put("routeOwnerModule", routeOwnerModule)
            route.moduleKey.orNull
                ?.takeIf(String::isNotBlank)
                ?.let { put("routeModuleKey", it) }
        }
    }

    companion object {
        const val PLUGIN_ID: String = "site.addzero.ksp.route"
        const val EXTENSION_NAME: String = "route"
        const val PROCESSOR_ARTIFACT_ID: String = "route-processor"
        const val COORDINATES_RESOURCE_PATH: String = "site/addzero/ksp/route/gradle-plugin.properties"
    }
}

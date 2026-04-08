package site.addzero.ksp

import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import site.addzero.gradle.kspconsumer.PublishedCompanionDependency
import site.addzero.gradle.kspconsumer.PublishedDependencyScope
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedKspConsumerDefinition
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact
import site.addzero.gradle.kspconsumer.configurePublishedKspConsumer
import site.addzero.gradle.kspconsumer.defaultPublishedKspSourceDirectory

abstract class RouteExtension {
    abstract val sharedSourceDir: Property<String>
    abstract val generatedPackage: Property<String>
    abstract val routeOwnerModule: Property<String>
    abstract val aggregationRole: Property<String>
    abstract val moduleKey: Property<String>

    init {
        sharedSourceDir.convention("")
        generatedPackage.convention("site.addzero.generated")
        routeOwnerModule.convention("")
        aggregationRole.convention("contributor")
        moduleKey.convention("")
    }
}

val publishedKspResourceClassLoader = Thread.currentThread().contextClassLoader ?: javaClass.classLoader
val routeExtension = extensions.create<RouteExtension>("route")

configurePublishedKspConsumer(
    definition = PublishedKspConsumerDefinition(
        pluginId = "site.addzero.ksp.route",
        coordinatesResourcePath = "site/addzero/ksp/route/gradle-plugin.properties",
        resourceClassLoader = publishedKspResourceClassLoader,
        processorArtifact = PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:route:route-processor",
            artifactId = "route-processor",
        ),
        companionDependencies = listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.KMP,
                localProjectPath = ":lib:ksp:route:route-core",
                artifactId = "route-core",
            ),
        ),
    ),
) {
    val sharedSourceDir = routeExtension.sharedSourceDir.orNull
        ?.takeIf(String::isNotBlank)
        ?: defaultPublishedKspSourceDirectory()
    val routeOwnerModule = routeExtension.routeOwnerModule.orNull
        ?.takeIf(String::isNotBlank)
        ?: defaultPublishedKspSourceDirectory()
    linkedMapOf<String, String>().apply {
        put("sharedSourceDir", sharedSourceDir)
        put("routeGenPkg", routeExtension.generatedPackage.get())
        put("routeOwnerModule", routeOwnerModule)
        put("routeAggregationRole", routeExtension.aggregationRole.get())
        routeExtension.moduleKey.orNull
            ?.takeIf(String::isNotBlank)
            ?.let { put("routeModuleKey", it) }
    }
}

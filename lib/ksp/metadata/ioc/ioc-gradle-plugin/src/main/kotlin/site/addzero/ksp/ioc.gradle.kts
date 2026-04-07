package site.addzero.ksp

import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import site.addzero.gradle.kspconsumer.PublishedCompanionDependency
import site.addzero.gradle.kspconsumer.PublishedDependencyScope
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedKspConsumerDefinition
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact
import site.addzero.gradle.kspconsumer.configurePublishedKspConsumer

abstract class IocExtension {
    abstract val modulePackage: Property<String>
    abstract val app: Property<Boolean>

    init {
        modulePackage.convention("")
        app.convention(false)
    }
}

val publishedKspResourceClassLoader = Thread.currentThread().contextClassLoader ?: javaClass.classLoader
val iocExtension = extensions.create<IocExtension>("ioc")

configurePublishedKspConsumer(
    definition = PublishedKspConsumerDefinition(
        pluginId = "site.addzero.ksp.ioc",
        coordinatesResourcePath = "site/addzero/ksp/ioc/gradle-plugin.properties",
        resourceClassLoader = publishedKspResourceClassLoader,
        processorArtifact = PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:ioc:ioc-processor",
            artifactId = "ioc-processor",
        ),
        companionDependencies = listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.KMP,
                localProjectPath = ":lib:ksp:metadata:ioc:ioc-core",
                artifactId = "ioc-core",
            ),
        ),
    ),
) {
    linkedMapOf<String, String>().apply {
        put("ioc.role", if (iocExtension.app.get()) "app" else "lib")
        iocExtension.modulePackage.orNull
            ?.takeIf(String::isNotBlank)
            ?.let { put("ioc.module", it) }
    }
}

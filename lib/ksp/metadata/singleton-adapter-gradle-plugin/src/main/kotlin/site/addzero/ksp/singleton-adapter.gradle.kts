package site.addzero.ksp

import site.addzero.gradle.kspconsumer.PublishedCompanionDependency
import site.addzero.gradle.kspconsumer.PublishedDependencyScope
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedKspConsumerDefinition
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact
import site.addzero.gradle.kspconsumer.configurePublishedKspConsumer

val publishedKspResourceClassLoader = Thread.currentThread().contextClassLoader ?: javaClass.classLoader

configurePublishedKspConsumer(
    definition = PublishedKspConsumerDefinition(
        pluginId = "site.addzero.ksp.singleton-adapter",
        coordinatesResourcePath = "site/addzero/ksp/singleton-adapter/gradle-plugin.properties",
        resourceClassLoader = publishedKspResourceClassLoader,
        processorArtifact = PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.JVM,
            localProjectPath = ":lib:ksp:metadata:singleton-adapter-processor",
            artifactId = "singleton-adapter-processor",
        ),
        companionDependencies = listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.JVM,
                localProjectPath = ":lib:ksp:metadata:singleton-adapter-api",
                artifactId = "singleton-adapter-api",
            ),
        ),
    ),
)

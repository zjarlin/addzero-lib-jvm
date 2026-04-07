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
        pluginId = "site.addzero.ksp.method-semanticizer",
        coordinatesResourcePath = "site/addzero/ksp/method-semanticizer/gradle-plugin.properties",
        resourceClassLoader = publishedKspResourceClassLoader,
        processorArtifact = PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:method-semanticizer:method-semanticizer-processor",
            artifactId = "method-semanticizer-processor",
        ),
        companionDependencies = listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.KMP,
                localProjectPath = ":lib:ksp:metadata:method-semanticizer:method-semanticizer-api",
                artifactId = "method-semanticizer-api",
            ),
        ),
    ),
)

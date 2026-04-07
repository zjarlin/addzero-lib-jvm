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
        pluginId = "site.addzero.ksp.ksp-dsl-builder",
        coordinatesResourcePath = "site/addzero/ksp/ksp-dsl-builder/gradle-plugin.properties",
        resourceClassLoader = publishedKspResourceClassLoader,
        processorArtifact = PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-processor",
            artifactId = "ksp-dsl-builder-processor",
        ),
        companionDependencies = listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.KMP,
                localProjectPath = ":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-core",
                artifactId = "ksp-dsl-builder-core",
            ),
        ),
    ),
)

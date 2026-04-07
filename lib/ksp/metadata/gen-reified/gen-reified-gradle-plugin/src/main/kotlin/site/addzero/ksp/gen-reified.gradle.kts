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
        pluginId = "site.addzero.ksp.gen-reified",
        coordinatesResourcePath = "site/addzero/ksp/gen-reified/gradle-plugin.properties",
        resourceClassLoader = publishedKspResourceClassLoader,
        processorArtifact = PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:gen-reified:gen-reified-processor",
            artifactId = "gen-reified-processor",
        ),
        companionDependencies = listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.KMP,
                localProjectPath = ":lib:ksp:metadata:gen-reified:gen-reified-core",
                artifactId = "gen-reified-core",
            ),
        ),
    ),
)

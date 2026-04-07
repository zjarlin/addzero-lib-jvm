package site.addzero.ksp

import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import site.addzero.gradle.kspconsumer.PublishedCompanionDependency
import site.addzero.gradle.kspconsumer.PublishedDependencyScope
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedKspConsumerDefinition
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact
import site.addzero.gradle.kspconsumer.configurePublishedKspConsumer

abstract class ComposePropsExtension {
    abstract val suffix: Property<String>

    init {
        suffix.convention("State")
    }
}

val publishedKspResourceClassLoader = Thread.currentThread().contextClassLoader ?: javaClass.classLoader
val composePropsExtension = extensions.create<ComposePropsExtension>("composeProps")

configurePublishedKspConsumer(
    definition = PublishedKspConsumerDefinition(
        pluginId = "site.addzero.ksp.compose-props",
        coordinatesResourcePath = "site/addzero/ksp/compose-props/gradle-plugin.properties",
        resourceClassLoader = publishedKspResourceClassLoader,
        processorArtifact = PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.KMP,
            localProjectPath = ":lib:ksp:metadata:compose-props:compose-props-processor",
            artifactId = "compose-props-processor",
        ),
        companionDependencies = listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.KMP,
                localProjectPath = ":lib:ksp:metadata:compose-props:compose-props-annotations",
                artifactId = "compose-props-annotations",
            ),
        ),
    ),
) {
    mapOf("COMPOSE_ATTRS_SUFFIX" to composePropsExtension.suffix.get())
}

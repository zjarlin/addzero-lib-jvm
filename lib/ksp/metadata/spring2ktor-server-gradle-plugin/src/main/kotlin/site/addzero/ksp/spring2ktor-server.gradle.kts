package site.addzero.ksp

import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import site.addzero.gradle.kspconsumer.PublishedCompanionDependency
import site.addzero.gradle.kspconsumer.PublishedDependencyScope
import site.addzero.gradle.kspconsumer.PublishedKspArtifactKind
import site.addzero.gradle.kspconsumer.PublishedKspConsumerDefinition
import site.addzero.gradle.kspconsumer.PublishedProcessorArtifact
import site.addzero.gradle.kspconsumer.configurePublishedKspConsumer

abstract class Spring2KtorServerExtension {
    abstract val generatedPackage: Property<String>

    init {
        generatedPackage.convention("")
    }
}

val publishedKspResourceClassLoader = Thread.currentThread().contextClassLoader ?: javaClass.classLoader
val spring2ktorServerExtension = extensions.create<Spring2KtorServerExtension>("spring2ktorServer")

configurePublishedKspConsumer(
    definition = PublishedKspConsumerDefinition(
        pluginId = "site.addzero.ksp.spring2ktor-server",
        coordinatesResourcePath = "site/addzero/ksp/spring2ktor-server/gradle-plugin.properties",
        resourceClassLoader = publishedKspResourceClassLoader,
        processorArtifact = PublishedProcessorArtifact(
            artifactKind = PublishedKspArtifactKind.JVM,
            localProjectPath = ":lib:ksp:metadata:spring2ktor-server-processor",
            artifactId = "spring2ktor-server-processor",
        ),
        companionDependencies = listOf(
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.IMPLEMENTATION,
                artifactKind = PublishedKspArtifactKind.JVM,
                localProjectPath = ":lib:ksp:metadata:spring2ktor-server-core",
                artifactId = "spring2ktor-server-core",
            ),
            PublishedCompanionDependency(
                scope = PublishedDependencyScope.COMPILE_ONLY,
                artifactKind = PublishedKspArtifactKind.JVM,
                notation = "org.springframework:spring-web:5.3.21",
            ),
        ),
    ),
) {
    spring2ktorServerExtension.generatedPackage.orNull
        ?.takeIf(String::isNotBlank)
        ?.let { mapOf("springKtor.generatedPackage" to it) }
        ?: emptyMap()
}

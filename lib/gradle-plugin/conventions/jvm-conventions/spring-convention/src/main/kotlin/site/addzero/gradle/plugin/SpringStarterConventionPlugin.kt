package site.addzero.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class SpringStarterConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.apply(plugin = "site.addzero.gradle.plugin.spring-common-convention")
        project.apply(plugin = "org.jetbrains.kotlin.jvm")
        project.apply(plugin = "java")
        project.apply(plugin = "org.jetbrains.kotlin.plugin.spring")

        val extension = project.extensions.getByType<SpringConventionExtension>()

        project.afterEvaluate {
            project.dependencies {
                // Standard Spring Starter dependencies
                add("compileOnly", "org.springframework.boot:spring-boot-starter-web")

                if (extension.includeAutoConfigure) {
                    add("compileOnly", "org.springframework.boot:spring-boot-autoconfigure")
                }

                if (extension.includeConfigurationProcessor) {
                    add("annotationProcessor", "org.springframework.boot:spring-boot-configuration-processor")
                }

                // Additional dependencies
                extension.additionalCompileOnlyDependencies.forEach { (dependency, _) ->
                    add("compileOnly", dependency)
                }

                extension.additionalAnnotationProcessors.forEach { (dependency, _) ->
                    add("annotationProcessor", dependency)
                }
            }

            // Configure Kotlin Spring plugin options
            project.tasks.withType<KotlinCompile>().configureEach {
                kotlinOptions {
                    freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all", "-Xspring-context-aware")
                }
            }
        }
    }
}
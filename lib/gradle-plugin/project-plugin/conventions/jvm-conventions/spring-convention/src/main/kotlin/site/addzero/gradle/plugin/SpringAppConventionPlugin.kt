package site.addzero.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class SpringAppConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.apply(plugin = "site.addzero.gradle.plugin.spring-common-convention")
        project.apply(plugin = "org.jetbrains.kotlin.jvm")
        project.apply(plugin = "java")
        project.apply(plugin = "org.jetbrains.kotlin.plugin.spring")
        project.apply(plugin = "io.spring.dependency-management")

        val extension = project.extensions.getByType<SpringConventionExtension>()

        project.afterEvaluate {
            project.dependencies {
                // Standard Spring App dependencies
                add("implementation", "org.springframework.boot:spring-boot-starter-web")

                // Additional dependencies
                extension.additionalDependencies.forEach { (dependency, _) ->
                    add("implementation", dependency)
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
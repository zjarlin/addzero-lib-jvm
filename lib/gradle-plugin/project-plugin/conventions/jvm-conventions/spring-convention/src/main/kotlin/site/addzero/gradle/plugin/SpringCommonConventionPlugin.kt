package site.addzero.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class SpringCommonConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.pluginManager.apply("java")

        val extension = project.extensions.create<SpringConventionExtension>("springConvention")

        project.afterEvaluate {
            project.dependencies {
                // Add Spring Boot BOM
                add("implementation", platform("org.springframework.boot:spring-boot-dependencies:${extension.springBootVersion}"))

                // Add standard test dependencies
                if (extension.includeStarterTest) {
                    add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
                }
                add("testImplementation", "org.junit.jupiter:junit-jupiter-api")
                add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine")

                if (extension.includeH2) {
                    add("testImplementation", "com.h2database:h2")
                }

                if (extension.includeWebInTest) {
                    add("testImplementation", "org.springframework.boot:spring-boot-starter-web")
                }

                // Add additional dependencies
                extension.additionalDependencies.forEach { (dependency, _) ->
                    add("implementation", dependency)
                }

                extension.additionalTestDependencies.forEach { (dependency, _) ->
                    add("testImplementation", dependency)
                }
            }

            // Configure Kotlin compiler options for Spring
            project.tasks.withType<KotlinCompile>().configureEach {
                kotlinOptions {
                    freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
                }
            }
        }
    }
}
package site.addzero.jimmer.lowquery.gradle

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue
import org.gradle.testkit.runner.GradleRunner

class JimmerLowQueryGradlePluginSmokeTest {
    @Test
    fun `plugin injects processor annotations and serialized args`() {
        val projectDir = Files.createTempDirectory("jimmer-low-query-plugin-smoke")
        projectDir.resolve("settings.gradle.kts").toFile().writeText("rootProject.name = \"consumer\"\n")
        projectDir.resolve("build.gradle.kts").toFile().writeText(buildScript())

        val output = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withPluginClasspath()
            .withArguments("verifyJimmerLowQueryPlugin", "--no-configuration-cache")
            .forwardOutput()
            .build()
            .output

        assertContains(output, "CONF[ksp]=site.addzero:jimmer-low-query-processor")
        assertContains(output, "CONF[implementation]=site.addzero:jimmer-low-query-annotations")
        assertContains(output, "jimmerLowQuery.generatedPackage=demo.generated.lowquery")
        assertContains(output, "build/generated/ksp/main/kotlin")
    }

    private fun buildScript(): String = """
        import org.gradle.api.Project
        import org.gradle.api.provider.Property

        plugins {
            kotlin("jvm") version "2.4.0"
            id("site.addzero.ksp.jimmer-low-query")
        }

        repositories {
            mavenCentral()
        }

        fun Project.dumpConfiguration(name: String): String =
            configurations.findByName(name)
                ?.dependencies
                ?.joinToString("|") { dependency ->
                    listOfNotNull(dependency.group, dependency.name).joinToString(":")
                }
                .orEmpty()

        fun Project.dumpJvmSourceDirs(): String =
            extensions.getByType(org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension::class.java)
                .sourceSets
                .getByName("main")
                .kotlin
                .srcDirs
                .map { projectDir.toPath().relativize(it.toPath()).toString() }
                .sorted()
                .joinToString("|")

        fun Any.setStringProperty(name: String, value: String) {
            val getterName = "get" + name.replaceFirstChar(Char::uppercase)
            val property = javaClass.methods
                .first { it.name == getterName && it.parameterCount == 0 }
                .invoke(this) as Property<String>
            property.set(value)
        }

        extensions.getByName("jimmerLowQuery")
            .setStringProperty("generatedPackage", "demo.generated.lowquery")

        tasks.register("verifyJimmerLowQueryPlugin") {
            doLast {
                println("CONF[ksp]=${'$'}{dumpConfiguration("ksp")}")
                println("CONF[implementation]=${'$'}{dumpConfiguration("implementation")}")
                println("SRC[jvm]=${'$'}{dumpJvmSourceDirs()}")
                println("ARGS=${'$'}{project.extensions.extraProperties.get("site.addzero.kspconsumer.site.addzero.ksp.jimmer-low-query.serializedArgs")}")
            }
        }
    """.trimIndent()

    private fun assertContains(output: String, expected: String) {
        assertTrue(output.contains(expected), "Expected output to contain `$expected`, actual output:\n$output")
    }
}

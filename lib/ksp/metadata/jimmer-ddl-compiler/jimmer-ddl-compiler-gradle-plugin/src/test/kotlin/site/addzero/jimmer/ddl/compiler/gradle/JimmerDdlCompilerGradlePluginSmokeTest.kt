package site.addzero.jimmer.ddl.compiler.gradle

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue
import org.gradle.testkit.runner.GradleRunner

class JimmerDdlCompilerGradlePluginSmokeTest {
    @Test
    fun `plugin injects ksp apt generated resources and serialized args`() {
        val projectDir = Files.createTempDirectory("jimmer-ddl-compiler-plugin-smoke")
        projectDir.resolve("settings.gradle.kts").toFile().writeText("rootProject.name = \"consumer\"\n")
        projectDir.resolve("build.gradle.kts").toFile().writeText(buildScript())

        val output = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withPluginClasspath()
            .withArguments("verifyJimmerDdlCompilerPlugin", "--no-configuration-cache")
            .forwardOutput()
            .build()
            .output

        assertContains(output, "CONF[ksp]=site.addzero:jimmer-ddl-compiler-processor")
        assertContains(output, "CONF[annotationProcessor]=site.addzero:jimmer-ddl-compiler-processor")
        assertContains(output, "SRC[resources]=build/generated/jimmer-ddl/main/resources")
        assertContains(output, "jimmerDdl.databaseType=h2")
        assertContains(output, "jimmerDdl.profiles=pluginSmoke")
        assertContains(output, "jimmerDdl.outputFormat=flyway")
        assertContains(output, "jimmerDdl.version=9100")
        assertContains(output, "jimmerDdl.description=plugin_smoke")
        assertContains(output, "jimmerDdl.includePackages=com.example.domain")
        assertContains(output, "jimmerDdl.excludePackages=com.example.domain.tmp")
    }

    private fun buildScript(): String = """
        import org.gradle.api.Project
        import org.gradle.api.provider.Property

        plugins {
            kotlin("jvm") version "2.4.0"
            id("site.addzero.ksp.jimmer-ddl-compiler")
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

        fun Project.dumpResourceDirs(): String =
            extensions.getByType(org.gradle.api.tasks.SourceSetContainer::class.java)
                .getByName("main")
                .resources
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

        extensions.getByName("jimmerDdl").apply {
            setStringProperty("databaseType", "h2")
            setStringProperty("profiles", "pluginSmoke")
            setStringProperty("outputFormat", "flyway")
            setStringProperty("version", "9100")
            setStringProperty("description", "plugin_smoke")
            setStringProperty("includePackages", "com.example.domain")
            setStringProperty("excludePackages", "com.example.domain.tmp")
        }

        tasks.register("verifyJimmerDdlCompilerPlugin") {
            doLast {
                println("CONF[ksp]=${'$'}{dumpConfiguration("ksp")}")
                println("CONF[annotationProcessor]=${'$'}{dumpConfiguration("annotationProcessor")}")
                println("SRC[resources]=${'$'}{dumpResourceDirs()}")
                println("ARGS=${'$'}{project.extensions.extraProperties.get("site.addzero.kspconsumer.site.addzero.ksp.jimmer-ddl-compiler.serializedArgs")}")
            }
        }
    """.trimIndent()

    private fun assertContains(output: String, expected: String) {
        assertTrue(output.contains(expected), "Expected output to contain `$expected`, actual output:\n$output")
    }
}

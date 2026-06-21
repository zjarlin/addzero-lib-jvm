package site.addzero.gradle.plugin

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

class PublishBuddyPluginTest {

    @Test
    fun `publish task includes recursive project dependencies declared via typesafe project accessors`() {
        val javaHome = System.getProperty("java.home")
        val testProjectDir = Files.createTempDirectory("publish-buddy-smoke")
        writeFile(testProjectDir, "settings.gradle.kts", settingsFile())
        writeFile(testProjectDir, "build.gradle.kts", rootBuildFile())
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(testProjectDir, "app/build.gradle.kts", appBuildFile())
        writeFile(testProjectDir, "dep/build.gradle.kts", depBuildFile())
        writeFile(testProjectDir, "leaf/build.gradle.kts", leafBuildFile())

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments(
                "--stacktrace",
                "--console=plain",
                ":app:publishToMavenCentral",
                "--dry-run",
            )
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"), result.output)
        assertInOrder(
            output = result.output,
            expectedSnippets = listOf(
                ":leaf:publishToMavenCentral SKIPPED",
                ":dep:publishToMavenCentral SKIPPED",
                ":app:publishToMavenCentral SKIPPED",
            ),
        )
    }

    @Test
    fun `test-only project dependencies do not create publish cycles`() {
        val javaHome = System.getProperty("java.home")
        val testProjectDir = Files.createTempDirectory("publish-buddy-test-scope")
        writeFile(testProjectDir, "settings.gradle.kts", settingsFile())
        writeFile(testProjectDir, "build.gradle.kts", rootBuildFile())
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(
            testProjectDir,
            "app/build.gradle.kts",
            """
                plugins {
                    `java-library`
                    id("site.addzero.gradle.plugin.publish-buddy")
                }

                dependencies {
                    implementation(projects.dep)
                }
            """.trimIndent(),
        )
        writeFile(
            testProjectDir,
            "dep/build.gradle.kts",
            """
                plugins {
                    `java-library`
                    id("site.addzero.gradle.plugin.publish-buddy")
                }

                dependencies {
                    testImplementation(projects.app)
                }
            """.trimIndent(),
        )
        writeFile(testProjectDir, "leaf/build.gradle.kts", leafBuildFile())

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments(
                "--stacktrace",
                "--console=plain",
                ":app:publishToMavenCentral",
                "--dry-run",
            )
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"), result.output)
        assertTrue(result.output.contains(":dep:publishToMavenCentral SKIPPED"), result.output)
        assertTrue(!result.output.contains("Circular dependency"), result.output)
    }

    @Test
    fun `artifact id can be composed from root to leaf project names`() {
        val javaHome = System.getProperty("java.home")
        val testProjectDir = Files.createTempDirectory("publish-buddy-artifact-name")
        writeFile(
            testProjectDir,
            "settings.gradle.kts",
            """
                rootProject.name = "system"

                dependencyResolutionManagement {
                    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
                    repositories {
                        mavenCentral()
                        gradlePluginPortal()
                    }
                }

                include(":auth")
            """.trimIndent(),
        )
        writeFile(
            testProjectDir,
            "build.gradle.kts",
            """
                import site.addzero.gradle.PublishConventionExtension

                plugins {
                    id("site.addzero.gradle.plugin.publish-buddy")
                }

                allprojects {
                    group = "site.addzero.test"
                    version = "1.0.0"
                }

                configure<PublishConventionExtension> {
                    useLeafName.set(false)
                }
            """.trimIndent(),
        )
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(
            testProjectDir,
            "auth/build.gradle.kts",
            """
                plugins {
                    `java-library`
                    id("site.addzero.gradle.plugin.publish-buddy")
                }
            """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments(
                "--stacktrace",
                "--console=plain",
                ":auth:generatePomFileForMavenPublication",
            )
            .withPluginClasspath()
            .forwardOutput()
            .build()

        val pomContent = testProjectDir
            .resolve("auth/build/publications/maven/pom-default.xml")
            .readText()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"), result.output)
        assertTrue(pomContent.contains("<groupId>site.addzero.test</groupId>"), pomContent)
        assertTrue(pomContent.contains("<artifactId>system-auth</artifactId>"), pomContent)
    }

    private fun assertInOrder(output: String, expectedSnippets: List<String>) {
        var lastIndex = -1
        expectedSnippets.forEach { snippet ->
            val index = output.indexOf(snippet)
            assertTrue(index >= 0, "Missing '$snippet' in output:\n$output")
            assertTrue(index > lastIndex, "Expected '$snippet' after previous snippets:\n$output")
            lastIndex = index
        }
    }

    private fun settingsFile(): String {
        return """
            rootProject.name = "publish-buddy-consumer"
            enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

            dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
                repositories {
                    mavenCentral()
                    gradlePluginPortal()
                }
            }

            include(":app")
            include(":dep")
            include(":leaf")
        """.trimIndent()
    }

    private fun rootBuildFile(): String {
        return """
            allprojects {
                group = "site.addzero.test"
                version = "1.0.0"
            }
        """.trimIndent()
    }

    private fun appBuildFile(): String {
        return """
            plugins {
                `java-library`
                id("site.addzero.gradle.plugin.publish-buddy")
            }

            dependencies {
                api(projects.dep)
            }
        """.trimIndent()
    }

    private fun depBuildFile(): String {
        return """
            plugins {
                `java-library`
                id("site.addzero.gradle.plugin.publish-buddy")
            }

            dependencies {
                api(projects.leaf)
            }
        """.trimIndent()
    }

    private fun leafBuildFile(): String {
        return """
            plugins {
                `java-library`
                id("site.addzero.gradle.plugin.publish-buddy")
            }
        """.trimIndent()
    }

    private fun gradleProperties(javaHome: String): String {
        val normalizedJavaHome = javaHome.replace("\\", "/")
        return """
            org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=768m -Dfile.encoding=UTF-8
            org.gradle.parallel=false
            org.gradle.configuration-cache=false
            org.gradle.java.home=$normalizedJavaHome
        """.trimIndent()
    }

    private fun writeFile(projectDir: Path, relativePath: String, content: String) {
        val target = projectDir.resolve(relativePath)
        target.parent?.createDirectories()
        target.writeText(content)
    }
}

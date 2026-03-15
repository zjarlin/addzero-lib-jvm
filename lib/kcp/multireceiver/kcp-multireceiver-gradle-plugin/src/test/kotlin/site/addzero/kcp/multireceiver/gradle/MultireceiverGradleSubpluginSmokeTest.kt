package site.addzero.kcp.multireceiver.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class MultireceiverGradleSubpluginSmokeTest {

    @Test
    fun compiles_consumer_project_and_exposes_generated_calls() {
        val javaHome = System.getProperty("java.home")
        val gradlePluginClasspath = System.getProperty("multireceiver.gradlePluginClasspath")
            ?: error("Missing multireceiver.gradlePluginClasspath system property")
        val localRepositoryDir = createLocalMavenRepository()
        val testProjectDir = Files.createTempDirectory("multireceiver-gradle-smoke")
        writeFile(testProjectDir, "settings.gradle.kts", settingsFile(localRepositoryDir))
        writeFile(testProjectDir, "build.gradle.kts", buildFile(gradlePluginClasspath))
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(
            testProjectDir,
            "src/main/kotlin/site/addzero/example/MultireceiverTargets.kt",
            """
                package site.addzero.example

                import site.addzero.kcp.annotations.GenerateExtension
                import site.addzero.kcp.annotations.Receiver

                data class Service(val prefix: String)

                @GenerateExtension
                fun wrap(value: String): String = "<${'$'}value>"

                @GenerateExtension
                fun render(@Receiver service: Service, value: Int): String =
                    "${'$'}{service.prefix}:${'$'}value"

                fun invoke(): String = buildString {
                    append("ok".wrap())
                    append("|")
                    append(context(Service("svc")) { render(3) })
                }
            """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments(
                "--stacktrace",
                "--console=plain",
                "compileKotlin",
            )
            .forwardOutput()
            .build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"), result.output)

        val targetsKt = loadClass(
            classesDir = testProjectDir.resolve("build/classes/kotlin/main"),
            name = "site.addzero.example.MultireceiverTargetsKt",
        )
        assertEquals("<ok>|svc:3", targetsKt.getMethod("invoke").invoke(null))
    }

    private fun settingsFile(localRepositoryDir: Path): String {
        return """
            import org.gradle.api.initialization.resolve.RepositoriesMode

            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    google()
                    mavenCentral()
                }
            }

            dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
                repositories {
                    maven(url = uri(${localRepositoryDir.toString().quoteForKotlin()}))
                    google()
                    mavenCentral()
                }
            }

            rootProject.name = "multireceiver-consumer"
        """.trimIndent()
    }

    private fun createLocalMavenRepository(): Path {
        val pluginGroup = System.getProperty("multireceiver.pluginGroup")
            ?: error("Missing multireceiver.pluginGroup system property")
        val pluginVersion = System.getProperty("multireceiver.pluginVersion")
            ?: error("Missing multireceiver.pluginVersion system property")
        val compilerPluginBuildDir = System.getProperty("multireceiver.compilerPluginBuildDir")
            ?.let(Paths::get)
            ?: error("Missing multireceiver.compilerPluginBuildDir system property")
        val annotationsBuildDir = System.getProperty("multireceiver.annotationsBuildDir")
            ?.let(Paths::get)
            ?: error("Missing multireceiver.annotationsBuildDir system property")
        val repositoryDir = Files.createTempDirectory("multireceiver-m2")
        installModule(
            repositoryDir = repositoryDir,
            groupId = pluginGroup,
            artifactId = "kcp-multireceiver-plugin",
            version = pluginVersion,
            jarFile = findPrimaryJar(compilerPluginBuildDir, "kcp-multireceiver-plugin"),
        )
        installModule(
            repositoryDir = repositoryDir,
            groupId = pluginGroup,
            artifactId = "kcp-multireceiver-annotations",
            version = pluginVersion,
            jarFile = findPrimaryJar(annotationsBuildDir, "kcp-multireceiver-annotations"),
        )
        return repositoryDir
    }

    private fun findPrimaryJar(buildDir: Path, artifactPrefix: String): Path {
        val libsDir = buildDir.resolve("libs")
        Files.list(libsDir).use { files ->
            val candidates = mutableListOf<Path>()
            files
                .filter { path ->
                    val name = path.fileName.toString()
                    Files.isRegularFile(path) &&
                        (name == "$artifactPrefix.jar" || name.startsWith("$artifactPrefix-")) &&
                        name.endsWith(".jar") &&
                        !name.endsWith("-sources.jar") &&
                        !name.endsWith("-javadoc.jar")
                }
                .forEach { path -> candidates.add(path) }
            return candidates
                .sortedWith(compareBy<Path> { path -> path.fileName.toString().length }
                    .thenBy { path -> path.fileName.toString() })
                .firstOrNull()
                ?: throw IllegalStateException("Missing primary jar for $artifactPrefix under $libsDir")
        }
    }

    private fun installModule(
        repositoryDir: Path,
        groupId: String,
        artifactId: String,
        version: String,
        jarFile: Path,
    ) {
        require(Files.isRegularFile(jarFile)) {
            "Missing artifact jar: $jarFile"
        }
        val moduleDir = repositoryDir
            .resolve(groupId.replace('.', '/'))
            .resolve(artifactId)
            .resolve(version)
            .createDirectories()
        Files.copy(
            jarFile,
            moduleDir.resolve("$artifactId-$version.jar"),
            StandardCopyOption.REPLACE_EXISTING,
        )
        moduleDir.resolve("$artifactId-$version.pom").writeText(
            """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>$groupId</groupId>
                  <artifactId>$artifactId</artifactId>
                  <version>$version</version>
                  <packaging>jar</packaging>
                </project>
            """.trimIndent(),
        )
    }

    private fun buildFile(gradlePluginClasspath: String): String {
        val classpathEntries = gradlePluginClasspath
            .split(File.pathSeparator)
            .filter(String::isNotBlank)
            .joinToString(separator = ",\n                        ") { path ->
                path.quoteForKotlin()
            }
        return """
            buildscript {
                dependencies {
                    classpath(
                        files(
                            $classpathEntries
                        )
                    )
                }
            }

            apply<org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper>()
            apply<site.addzero.kcp.multireceiver.gradle.MultireceiverGradleSubplugin>()

            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
                compilerOptions.freeCompilerArgs.set(listOf("-Xjsr305=strict"))
            }
        """.trimIndent()
    }

    private fun gradleProperties(javaHome: String): String {
        return """
            org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=768m -Dfile.encoding=UTF-8
            org.gradle.parallel=false
            org.gradle.configuration-cache=false
            org.gradle.java.home=$javaHome
        """.trimIndent()
    }

    private fun writeFile(projectDir: Path, relativePath: String, content: String) {
        val file = projectDir.resolve(relativePath)
        file.parent.createDirectories()
        file.writeText(content)
    }

    private fun loadClass(classesDir: Path, name: String): Class<*> {
        val kotlinStdlib = findClasspathEntry("kotlin-stdlib")
        val kotlinStdlibJdk8 = findClasspathEntry("kotlin-stdlib-jdk8")
        val urls = listOfNotNull(
            classesDir.toUri().toURL(),
            kotlinStdlib?.toURI()?.toURL(),
            kotlinStdlibJdk8?.toURI()?.toURL(),
        ).toTypedArray()
        val classLoader = URLClassLoader(urls, javaClass.classLoader)
        return classLoader.loadClass(name)
    }

    private fun findClasspathEntry(fragment: String): File? {
        return System.getProperty("java.class.path")
            .split(File.pathSeparator)
            .map(::File)
            .firstOrNull { file -> file.name.contains(fragment) }
    }

    private fun String.quoteForKotlin(): String {
        return "\"" + replace("\\", "\\\\") + "\""
    }
}

package site.addzero.kcp.transformoverload.gradle

import org.gradle.testkit.runner.GradleRunner
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

class TransformOverloadGradleSubpluginSmokeTest {

    @Test
    fun compiles_consumer_project_and_exposes_generated_methods() {
        val javaHome = System.getProperty("java.home")
        val gradlePluginClasspath = System.getProperty("transformOverload.gradlePluginClasspath")
            ?: error("Missing transformOverload.gradlePluginClasspath system property")
        val localRepositoryDir = createLocalMavenRepository()
        val testProjectDir = Files.createTempDirectory("transform-overload-gradle-smoke")
        writeFile(testProjectDir, "settings.gradle.kts", settingsFile(localRepositoryDir))
        writeFile(testProjectDir, "build.gradle.kts", buildFile(gradlePluginClasspath))
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(
            testProjectDir,
            "src/main/kotlin/org/babyfish/jimmer/Stubs.kt",
            """
                package org.babyfish.jimmer

                interface Input<E> {
                    fun toEntity(): E
                }
            """.trimIndent(),
        )
        writeFile(
            testProjectDir,
            "src/main/kotlin/org/babyfish/jimmer/spring/repo/RepositoryTargets.kt",
            """
                package org.babyfish.jimmer.spring.repo

                import org.babyfish.jimmer.Input
                import site.addzero.kcp.transformoverload.annotations.GenerateTransformOverloads
                import site.addzero.kcp.transformoverload.annotations.OverloadTransform

                @OverloadTransform
                fun <E : Any> Input<E>.toEntityInput(): E = toEntity()

                @GenerateTransformOverloads
                interface KotlinRepository<E : Any> {
                    fun save(value: E): String
                }

                fun verify(repo: KotlinRepository<String>, value: Input<String>) {
                    repo.save(value)
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

        val repositoryClass = loadClass(
            classesDir = testProjectDir.resolve("build/classes/kotlin/main"),
            name = "org.babyfish.jimmer.spring.repo.KotlinRepository",
        )
        assertTrue(
            repositoryClass.declaredMethods.any { method ->
                method.name == "save" &&
                    method.parameterTypes.singleOrNull()?.name == "org.babyfish.jimmer.Input"
            },
            repositoryClass.declaredMethods.joinToString("\n"),
        )
    }

    private fun settingsFile(localRepositoryDir: Path): String {
        return """
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

            rootProject.name = "transform-overload-consumer"
        """.trimIndent()
    }

    private fun createLocalMavenRepository(): Path {
        val pluginGroup = System.getProperty("transformOverload.pluginGroup")
            ?: error("Missing transformOverload.pluginGroup system property")
        val pluginVersion = System.getProperty("transformOverload.pluginVersion")
            ?: error("Missing transformOverload.pluginVersion system property")
        val compilerPluginBuildDir = System.getProperty("transformOverload.compilerPluginBuildDir")
            ?.let(Paths::get)
            ?: error("Missing transformOverload.compilerPluginBuildDir system property")
        val annotationsBuildDir = System.getProperty("transformOverload.annotationsBuildDir")
            ?.let(Paths::get)
            ?: error("Missing transformOverload.annotationsBuildDir system property")
        val repositoryDir = Files.createTempDirectory("transform-overload-m2")
        installModule(
            repositoryDir = repositoryDir,
            groupId = pluginGroup,
            artifactId = "kcp-transform-overload-plugin",
            version = pluginVersion,
            jarFile = findPrimaryJar(compilerPluginBuildDir, "kcp-transform-overload-plugin"),
        )
        installModule(
            repositoryDir = repositoryDir,
            groupId = pluginGroup,
            artifactId = "kcp-transform-overload-annotations",
            version = pluginVersion,
            jarFile = findPrimaryJar(annotationsBuildDir, "kcp-transform-overload-annotations"),
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
            apply<site.addzero.kcp.transformoverload.gradle.TransformOverloadGradleSubplugin>()
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

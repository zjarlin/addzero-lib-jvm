package site.addzero.kcp.allobjectjvmstatic.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class AllObjectJvmStaticGradleSubpluginSmokeTest {

    @Test
    fun compiles_consumer_project_and_exposes_static_methods_for_object_and_companion() {
        val javaHome = System.getProperty("java.home")
        val gradlePluginClasspath = System.getProperty("allObjectJvmStatic.gradlePluginClasspath")
            ?: error("Missing allObjectJvmStatic.gradlePluginClasspath system property")
        val localRepositoryDir = createLocalMavenRepository()
        val testProjectDir = Files.createTempDirectory("all-object-jvm-static-gradle-smoke")
        writeFile(testProjectDir, "settings.gradle.kts", settingsFile(localRepositoryDir))
        writeFile(testProjectDir, "build.gradle.kts", buildFile(gradlePluginClasspath))
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(
            testProjectDir,
            "src/main/kotlin/sample/AutoWhereUtil.kt",
            """
                package sample

                object AutoWhereUtil {
                    fun greet(name: String): String = "Hello, ${'$'}name"
                }

                class CompanionHolder {
                    companion object {
                        fun join(left: String, right: String): String = "${'$'}left-${'$'}right"
                    }
                }
            """.trimIndent(),
        )
        writeFile(
            testProjectDir,
            "src/main/java/sample/JavaConsumer.java",
            """
                package sample;

                public final class JavaConsumer {
                    private JavaConsumer() {
                    }

                    public static String callUtil() {
                        return AutoWhereUtil.greet("Java");
                    }

                    public static String callCompanion() {
                        return CompanionHolder.join("A", "B");
                    }
                }
            """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments(
                "--stacktrace",
                "--console=plain",
                "classes",
            )
            .forwardOutput()
            .build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"), result.output)

        val classLoader = createClassLoader(testProjectDir)
        val autoWhereUtilClass = classLoader.loadClass("sample.AutoWhereUtil")
        val companionHolderClass = classLoader.loadClass("sample.CompanionHolder")
        val javaConsumerClass = classLoader.loadClass("sample.JavaConsumer")

        val greet = autoWhereUtilClass.getDeclaredMethod("greet", String::class.java)
        val join = companionHolderClass.getDeclaredMethod("join", String::class.java, String::class.java)
        assertTrue(Modifier.isStatic(greet.modifiers), "Expected AutoWhereUtil.greet to be static")
        assertTrue(Modifier.isStatic(join.modifiers), "Expected CompanionHolder.join to be static")

        val utilResult = javaConsumerClass.getDeclaredMethod("callUtil").invoke(null)
        val companionResult = javaConsumerClass.getDeclaredMethod("callCompanion").invoke(null)
        assertEquals("Hello, Java", utilResult)
        assertEquals("A-B", companionResult)
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

            rootProject.name = "all-object-jvm-static-consumer"
        """.trimIndent()
    }

    private fun createLocalMavenRepository(): Path {
        val pluginGroup = System.getProperty("allObjectJvmStatic.pluginGroup")
            ?: error("Missing allObjectJvmStatic.pluginGroup system property")
        val pluginVersion = System.getProperty("allObjectJvmStatic.pluginVersion")
            ?: error("Missing allObjectJvmStatic.pluginVersion system property")
        val compilerPluginBuildDir = System.getProperty("allObjectJvmStatic.compilerPluginBuildDir")
            ?.let(Paths::get)
            ?: error("Missing allObjectJvmStatic.compilerPluginBuildDir system property")
        val repositoryDir = Files.createTempDirectory("all-object-jvm-static-m2")
        installModule(
            repositoryDir = repositoryDir,
            groupId = pluginGroup,
            artifactId = "kcp-all-object-jvm-static-plugin",
            version = pluginVersion,
            jarFile = findPrimaryJar(compilerPluginBuildDir, "kcp-all-object-jvm-static-plugin"),
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
            apply<site.addzero.kcp.allobjectjvmstatic.gradle.AllObjectJvmStaticGradleSubplugin>()
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
        val file = projectDir.resolve(relativePath)
        file.parent.createDirectories()
        file.writeText(content)
    }

    private fun createClassLoader(projectDir: Path): URLClassLoader {
        val kotlinClassesDir = projectDir.resolve("build/classes/kotlin/main")
        val javaClassesDir = projectDir.resolve("build/classes/java/main")
        val kotlinStdlib = findClasspathEntry("kotlin-stdlib")
        val kotlinStdlibJdk8 = findClasspathEntry("kotlin-stdlib-jdk8")
        val urls = listOfNotNull(
            kotlinClassesDir.toUri().toURL(),
            javaClassesDir.toUri().toURL(),
            kotlinStdlib?.toURI()?.toURL(),
            kotlinStdlibJdk8?.toURI()?.toURL(),
        ).toTypedArray()
        return URLClassLoader(urls, javaClass.classLoader)
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

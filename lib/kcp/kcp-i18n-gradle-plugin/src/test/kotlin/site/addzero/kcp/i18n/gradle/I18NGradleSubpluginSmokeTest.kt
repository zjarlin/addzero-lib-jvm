package site.addzero.kcp.i18n.gradle

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

class I18NGradleSubpluginSmokeTest {

    @Test
    fun compiles_consumer_project_via_plugin_id_and_resolves_runtime_translation() {
        val javaHome = System.getProperty("java.home")
        val gradlePluginClasspath = System.getProperty("i18n.gradlePluginClasspath")
            ?: error("Missing i18n.gradlePluginClasspath system property")
        val localRepositoryDir = createLocalMavenRepository()
        val testProjectDir = Files.createTempDirectory("i18n-gradle-smoke")
        writeFile(testProjectDir, "settings.gradle.kts", settingsFile(localRepositoryDir))
        writeFile(testProjectDir, "build.gradle.kts", buildFile(gradlePluginClasspath))
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(
            testProjectDir,
            "src/main/kotlin/site/addzero/example/I18NTargets.kt",
            """
                package site.addzero.example

                fun helloMessage(): String = "你好"

                fun invoke(): String = helloMessage()
            """.trimIndent(),
        )
        writeFile(
            testProjectDir,
            "src/main/resources/i18n/en.properties",
            """
                I18NTargets_helloMessage_text_你好=hello
            """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments(
                "--stacktrace",
                "--console=plain",
                "build",
            )
            .forwardOutput()
            .build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"), result.output)

        val targetsKt = loadClass(
            classesDir = testProjectDir.resolve("build/classes/kotlin/main"),
            resourcesDir = testProjectDir.resolve("build/resources/main"),
            name = "site.addzero.example.I18NTargetsKt",
        )
        assertEquals("hello", targetsKt.getMethod("invoke").invoke(null))
    }

    private fun settingsFile(localRepositoryDir: Path): String {
        return """
            import org.gradle.api.initialization.resolve.RepositoriesMode

            dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
                repositories {
                    maven(url = uri(${localRepositoryDir.toString().quoteForKotlin()}))
                }
            }

            rootProject.name = "i18n-consumer"
        """.trimIndent()
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
            apply(plugin = "site.addzero.kcp.i18n")

            configure<site.addzero.kcp.i18n.gradle.I18NGradleExtension> {
                targetLocale.set("en")
                resourceBasePath.set("i18n")
            }
        """.trimIndent()
    }

    private fun createLocalMavenRepository(): Path {
        val pluginGroup = System.getProperty("i18n.pluginGroup")
            ?: error("Missing i18n.pluginGroup system property")
        val pluginVersion = System.getProperty("i18n.pluginVersion")
            ?: error("Missing i18n.pluginVersion system property")
        val compilerPluginBuildDir = System.getProperty("i18n.compilerPluginBuildDir")
            ?.let(Paths::get)
            ?: error("Missing i18n.compilerPluginBuildDir system property")
        val runtimeBuildDir = System.getProperty("i18n.runtimeBuildDir")
            ?.let(Paths::get)
            ?: error("Missing i18n.runtimeBuildDir system property")
        val gradlePluginClasspath = System.getProperty("i18n.gradlePluginClasspath")
            ?: error("Missing i18n.gradlePluginClasspath system property")
        val repositoryDir = Files.createTempDirectory("i18n-m2")
        installModule(
            repositoryDir = repositoryDir,
            groupId = pluginGroup,
            artifactId = "kcp-i18n",
            version = pluginVersion,
            jarFile = findPrimaryJar(compilerPluginBuildDir, "kcp-i18n"),
        )
        installModule(
            repositoryDir = repositoryDir,
            groupId = pluginGroup,
            artifactId = "kcp-i18n-runtime",
            version = pluginVersion,
            jarFile = findPrimaryJar(runtimeBuildDir, "kcp-i18n-runtime"),
        )
        mirrorGradleClasspathArtifacts(repositoryDir, gradlePluginClasspath)
        mirrorGradleCacheGroup(repositoryDir, "org.jetbrains")
        mirrorGradleCacheGroup(repositoryDir, "org.jetbrains.intellij.deps")
        mirrorGradleCacheGroup(repositoryDir, "org.jetbrains.kotlin")
        mirrorGradleCacheGroup(repositoryDir, "org.jetbrains.kotlinx")
        return repositoryDir
    }

    private fun mirrorGradleClasspathArtifacts(repositoryDir: Path, classpath: String) {
        classpath
            .split(File.pathSeparator)
            .filter(String::isNotBlank)
            .map(Paths::get)
            .filter(Files::isRegularFile)
            .mapNotNull(::toGradleCacheCoordinates)
            .distinct()
            .forEach { coordinates ->
                copyGradleCacheVersionArtifacts(
                    repositoryDir = repositoryDir,
                    groupId = coordinates.groupId,
                    artifactId = coordinates.artifactId,
                    version = coordinates.version,
                )
            }
    }

    private fun mirrorGradleCacheGroup(
        repositoryDir: Path,
        groupId: String,
    ) {
        val groupDir = gradleCacheFilesDir().resolve(groupId)
        if (!Files.isDirectory(groupDir)) {
            return
        }
        Files.list(groupDir).use { artifactDirs ->
            artifactDirs
                .filter(Files::isDirectory)
                .forEach { artifactDir ->
                    Files.list(artifactDir).use { versionDirs ->
                        versionDirs
                            .filter(Files::isDirectory)
                            .forEach { versionDir ->
                                copyGradleCacheVersionArtifacts(
                                    repositoryDir = repositoryDir,
                                    groupId = groupId,
                                    artifactId = artifactDir.fileName.toString(),
                                    version = versionDir.fileName.toString(),
                                )
                            }
                    }
                }
        }
    }

    private fun copyGradleCacheVersionArtifacts(
        repositoryDir: Path,
        groupId: String,
        artifactId: String,
        version: String,
    ) {
        val sourceVersionDir = gradleCacheFilesDir()
            .resolve(groupId)
            .resolve(artifactId)
            .resolve(version)
        if (!Files.isDirectory(sourceVersionDir)) {
            return
        }
        val targetDir = repositoryDir
            .resolve(groupId.replace('.', '/'))
            .resolve(artifactId)
            .resolve(version)
            .createDirectories()
        Files.walk(sourceVersionDir, 2).use { files ->
            files
                .filter(Files::isRegularFile)
                .forEach { file ->
                    Files.copy(
                        file,
                        targetDir.resolve(file.fileName.toString()),
                        StandardCopyOption.REPLACE_EXISTING,
                    )
                }
        }
    }

    private fun toGradleCacheCoordinates(file: Path): CacheCoordinates? {
        val normalizedFile = file.toAbsolutePath().normalize()
        val filesDir = gradleCacheFilesDir()
        if (!normalizedFile.startsWith(filesDir)) {
            return null
        }
        val relative = filesDir.relativize(normalizedFile)
        if (relative.nameCount < 5) {
            return null
        }
        val groupId = buildString {
            for (index in 0 until relative.nameCount - 4) {
                if (isNotEmpty()) {
                    append('.')
                }
                append(relative.getName(index).toString())
            }
        }
        return CacheCoordinates(
            groupId = groupId,
            artifactId = relative.getName(relative.nameCount - 4).toString(),
            version = relative.getName(relative.nameCount - 3).toString(),
        )
    }

    private fun gradleCacheFilesDir(): Path {
        return Paths.get(System.getProperty("user.home"))
            .resolve(".gradle/caches/modules-2/files-2.1")
    }

    private data class CacheCoordinates(
        val groupId: String,
        val artifactId: String,
        val version: String,
    )

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
        dependencies: List<Triple<String, String, String>> = emptyList(),
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
        val dependenciesXml = if (dependencies.isEmpty()) {
            ""
        } else {
            dependencies.joinToString(
                prefix = "<dependencies>",
                postfix = "</dependencies>",
            ) { (depGroup, depArtifact, depVersion) ->
                """
                    <dependency>
                      <groupId>$depGroup</groupId>
                      <artifactId>$depArtifact</artifactId>
                      <version>$depVersion</version>
                    </dependency>
                """.trimIndent()
            }
        }
        moduleDir.resolve("$artifactId-$version.pom").writeText(
            """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>$groupId</groupId>
                  <artifactId>$artifactId</artifactId>
                  <version>$version</version>
                  <packaging>jar</packaging>
                  $dependenciesXml
                </project>
            """.trimIndent(),
        )
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

    private fun loadClass(
        classesDir: Path,
        resourcesDir: Path,
        name: String,
    ): Class<*> {
        val runtimeBuildDir = System.getProperty("i18n.runtimeBuildDir")
            ?.let(Paths::get)
            ?: error("Missing i18n.runtimeBuildDir system property")
        val runtimeJar = findPrimaryJar(runtimeBuildDir, "kcp-i18n-runtime")
        val kotlinStdlib = findClasspathEntry("kotlin-stdlib")
        val kotlinStdlibJdk8 = findClasspathEntry("kotlin-stdlib-jdk8")
        val urls = listOfNotNull(
            classesDir.toUri().toURL(),
            resourcesDir.toUri().toURL(),
            runtimeJar.toUri().toURL(),
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

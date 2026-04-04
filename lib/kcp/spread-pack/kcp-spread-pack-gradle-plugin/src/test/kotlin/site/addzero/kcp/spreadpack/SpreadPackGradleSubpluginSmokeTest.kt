package site.addzero.kcp.spreadpack

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

class SpreadPackGradleSubpluginSmokeTest {

    @Test
    fun compiles_consumer_project_and_runs_generated_overload() {
        val javaHome = System.getProperty("java.home")
        val localRepositoryDir = createLocalMavenRepository()
        val testProjectDir = Files.createTempDirectory("spread-pack-gradle-smoke")
        writeFile(testProjectDir, "settings.gradle.kts", settingsFile(localRepositoryDir))
        writeFile(testProjectDir, "build.gradle.kts", inlineConsumerBuildFile())
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(
            testProjectDir,
            "src/main/kotlin/site/addzero/example/SpreadPackConsumer.kt",
            """
                package site.addzero.example

                import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
                import site.addzero.kcp.spreadpack.SpreadPack
                import site.addzero.kcp.spreadpack.SpreadPackSelector

                data class FormOptions(
                    val name: String = "guest",
                    val enabled: Boolean = false,
                    val onDone: (() -> String)? = null,
                )

                @GenerateSpreadPackOverloads
                class ConsumerService {
                    fun submit(
                        @SpreadPack(selector = SpreadPackSelector.ATTRS)
                        options: FormOptions,
                    ): String {
                        val done = options.onDone?.invoke() ?: "-"
                        return "${'$'}{options.name}:${'$'}{options.enabled}:${'$'}done"
                    }
                }

                fun invokeGenerated(): String =
                    ConsumerService().submit(
                        name = "gradle",
                        enabled = true,
                    )
            """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withPluginClasspath()
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments(
                "--stacktrace",
                "--console=plain",
                "compileKotlin",
            )
            .forwardOutput()
            .build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"), result.output)

        val classLoader = createClassLoader(testProjectDir)
        val consumerKt = classLoader.loadClass("site.addzero.example.SpreadPackConsumerKt")
        assertEquals("gradle:true:-", consumerKt.getDeclaredMethod("invokeGenerated").invoke(null))
    }

    @Test
    fun committed_example_project_builds_and_runs_with_plugin_id() {
        val javaHome = System.getProperty("java.home")
        val localRepositoryDir = createLocalMavenRepository()
        val testProjectDir = Files.createTempDirectory("spread-pack-example-smoke")
        copyCommittedExampleSources(testProjectDir)
        copyCommittedBuildScript(testProjectDir)
        writeFile(
            testProjectDir,
            "settings.gradle.kts",
            committedExampleSettingsFile(localRepositoryDir),
        )
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withPluginClasspath()
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments(
                "--stacktrace",
                "--console=plain",
                "clean",
                "test",
                "run",
            )
            .forwardOutput()
            .build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"), result.output)
        assertTrue(
            result.output.contains(
                "TextProps[text,color,maxLines,softWrap,onTextLayout]=(hello,blue,2,false,callback-fixed)|" +
                    "Text(text=[MyText] world,color=red,maxLines=3,softWrap=true,layout=wrapped-layout)",
            ),
            result.output,
        )

        val classLoader = createClassLoader(testProjectDir)
        val exampleKt = classLoader.loadClass("site.addzero.example.SpreadPackExampleKt")
        assertEquals(
            "TextProps[text,color,maxLines,softWrap,onTextLayout]=(hello,blue,2,false,callback-fixed)|" +
                "Text(text=[MyText] world,color=red,maxLines=3,softWrap=true,layout=wrapped-layout)",
            exampleKt.getDeclaredMethod("invokeSpreadPackExample").invoke(null),
        )
    }

    @Test
    fun compiles_kmp_common_main_metadata_with_annotated_carrier_properties() {
        val javaHome = System.getProperty("java.home")
        val localRepositoryDir = createLocalMavenRepository()
        val testProjectDir = Files.createTempDirectory("spread-pack-kmp-metadata")
        writeFile(testProjectDir, "settings.gradle.kts", settingsFile(localRepositoryDir))
        writeFile(testProjectDir, "build.gradle.kts", multiplatformConsumerBuildFile())
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(
            testProjectDir,
            "src/commonMain/kotlin/site/addzero/example/SpreadPackCommonMain.kt",
            """
                package site.addzero.example

                import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
                import site.addzero.kcp.spreadpack.SpreadPack
                import site.addzero.kcp.spreadpack.SpreadPackCarrierOf

                fun renderBase(
                    title: String = "untitled",
                    count: Int = 0,
                    debug: Boolean = false,
                ): String = "${'$'}title:${'$'}count:${'$'}debug"

                @SpreadPackCarrierOf(
                    "site.addzero.example.renderBase",
                    exclude = ["debug"],
                )
                class RenderAliasArgs

                @GenerateSpreadPackOverloads
                fun renderAlias(
                    @SpreadPack
                    args: RenderAliasArgs,
                ): String = renderBase(
                    title = args.title,
                    count = args.count,
                    debug = true,
                )
            """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withPluginClasspath()
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments(
                "--stacktrace",
                "--console=plain",
                "compileCommonMainKotlinMetadata",
            )
            .forwardOutput()
            .build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"), result.output)
    }

    @Test
    fun compiles_kmp_common_main_metadata_with_compose_text_carrier() {
        val javaHome = System.getProperty("java.home")
        val localRepositoryDir = createLocalMavenRepository()
        val testProjectDir = Files.createTempDirectory("spread-pack-compose-text")
        writeFile(testProjectDir, "settings.gradle.kts", settingsFile(localRepositoryDir))
        writeFile(testProjectDir, "build.gradle.kts", composeCarrierConsumerBuildFile())
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(
            testProjectDir,
            "src/commonMain/kotlin/site/addzero/example/SpreadPackComposeText.kt",
            """
                package site.addzero.example

                import androidx.compose.foundation.text.TextAutoSize
                import androidx.compose.ui.Modifier
                import androidx.compose.ui.graphics.Color
                import androidx.compose.ui.text.TextLayoutResult
                import androidx.compose.ui.text.TextStyle
                import androidx.compose.ui.text.font.FontFamily
                import androidx.compose.ui.text.font.FontStyle
                import androidx.compose.ui.text.font.FontWeight
                import androidx.compose.ui.text.style.TextAlign
                import androidx.compose.ui.text.style.TextDecoration
                import androidx.compose.ui.text.style.TextOverflow
                import androidx.compose.ui.unit.TextUnit
                import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
                import site.addzero.kcp.spreadpack.SpreadPack
                import site.addzero.kcp.spreadpack.SpreadPackCarrierOf

                @SpreadPackCarrierOf(
                    value = "androidx.compose.material3.Text",
                    parameterTypes = [
                        String::class,
                        Modifier::class,
                        Color::class,
                        TextAutoSize::class,
                        TextUnit::class,
                        FontStyle::class,
                        FontWeight::class,
                        FontFamily::class,
                        TextUnit::class,
                        TextDecoration::class,
                        TextAlign::class,
                        TextUnit::class,
                        TextOverflow::class,
                        Boolean::class,
                        Int::class,
                        Int::class,
                        Function1::class,
                        TextStyle::class,
                    ],
                    exclude = [
                        "autoSize",
                        "fontSize",
                        "fontStyle",
                        "fontWeight",
                        "fontFamily",
                        "letterSpacing",
                        "textDecoration",
                        "lineHeight",
                        "overflow",
                        "softWrap",
                        "maxLines",
                        "minLines",
                        "onTextLayout",
                        "style",
                    ],
                )
                class M3TextArgs

                @GenerateSpreadPackOverloads
                fun renderAlias(
                    @SpreadPack
                    args: M3TextArgs,
                ): String = "${'$'}{args.text}:${'$'}{args.modifier}:${'$'}{args.color}:${'$'}{args.textAlign}"
            """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withPluginClasspath()
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments(
                "--stacktrace",
                "--console=plain",
                "compileCommonMainKotlinMetadata",
            )
            .forwardOutput()
            .build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"), result.output)
    }

    @Test
    fun compiles_kmp_common_main_metadata_with_local_named_default_values() {
        val javaHome = System.getProperty("java.home")
        val localRepositoryDir = createLocalMavenRepository()
        val testProjectDir = Files.createTempDirectory("spread-pack-local-defaults")
        writeFile(testProjectDir, "settings.gradle.kts", settingsFile(localRepositoryDir))
        writeFile(testProjectDir, "build.gradle.kts", multiplatformConsumerBuildFile())
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(
            testProjectDir,
            "src/commonMain/kotlin/site/addzero/example/SpreadPackLocalDefaults.kt",
            """
                package site.addzero.example

                import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
                import site.addzero.kcp.spreadpack.SpreadPack

                val DefaultLabel = "default"

                data class RenderArgs(
                    val title: String,
                    val label: String = DefaultLabel,
                )

                @GenerateSpreadPackOverloads
                fun render(
                    @SpreadPack
                    args: RenderArgs,
                ): String = "${'$'}{args.title}:${'$'}{args.label}"
            """.trimIndent(),
        )

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withPluginClasspath()
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments(
                "--stacktrace",
                "--console=plain",
                "compileCommonMainKotlinMetadata",
            )
            .forwardOutput()
            .build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"), result.output)
    }

    private fun settingsFile(localRepositoryDir: Path): String {
        return """
            pluginManagement {
                repositories {
                    maven(url = uri(${localRepositoryDir.toString().quoteForKotlin()}))
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

            rootProject.name = "spread-pack-consumer"
        """.trimIndent()
    }

    private fun committedExampleSettingsFile(localRepositoryDir: Path): String {
        val repoRoot = System.getProperty("spreadPack.repoRoot")
            ?.let(Paths::get)
            ?: error("Missing spreadPack.repoRoot system property")
        val libsToml = repoRoot
            .resolve("checkouts/build-logic/gradle/libs.versions.toml")
            .normalize()
        require(Files.isRegularFile(libsToml)) {
            "Missing version catalog: $libsToml"
        }
        return """
            pluginManagement {
                repositories {
                    maven(url = uri(${localRepositoryDir.toString().quoteForKotlin()}))
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
                versionCatalogs {
                    create("libs") {
                        from(files(${libsToml.toString().quoteForKotlin()}))
                    }
                }
            }

            rootProject.name = "example-spread-pack"
        """.trimIndent()
    }

    private fun createLocalMavenRepository(): Path {
        val pluginGroup = System.getProperty("spreadPack.pluginGroup")
            ?: error("Missing spreadPack.pluginGroup system property")
        val pluginVersion = System.getProperty("spreadPack.pluginVersion")
            ?: error("Missing spreadPack.pluginVersion system property")
        val compilerPluginBuildDir = System.getProperty("spreadPack.compilerPluginBuildDir")
            ?.let(Paths::get)
            ?: error("Missing spreadPack.compilerPluginBuildDir system property")
        val annotationsBuildDir = System.getProperty("spreadPack.annotationsBuildDir")
            ?.let(Paths::get)
            ?: error("Missing spreadPack.annotationsBuildDir system property")
        val repositoryDir = Files.createTempDirectory("spread-pack-m2")
        installModule(
            repositoryDir = repositoryDir,
            groupId = pluginGroup,
            artifactId = "kcp-spread-pack-plugin",
            version = pluginVersion,
            jarFile = findPrimaryJar(compilerPluginBuildDir, "kcp-spread-pack-plugin"),
        )
        installKotlinMultiplatformAnnotationsModule(
            repositoryDir = repositoryDir,
            groupId = pluginGroup,
            version = pluginVersion,
            annotationsBuildDir = annotationsBuildDir,
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

    private fun installKotlinMultiplatformAnnotationsModule(
        repositoryDir: Path,
        groupId: String,
        version: String,
        annotationsBuildDir: Path,
    ) {
        val metadataPublicationDir = annotationsBuildDir.resolve("publications/kotlinMultiplatform")
        val metadataModuleDir = repositoryDir
            .resolve(groupId.replace('.', '/'))
            .resolve("kcp-spread-pack-annotations")
            .resolve(version)
            .createDirectories()
        Files.copy(
            metadataPublicationDir.resolve("module.json"),
            metadataModuleDir.resolve("kcp-spread-pack-annotations-$version.module"),
            StandardCopyOption.REPLACE_EXISTING,
        )
        Files.copy(
            metadataPublicationDir.resolve("pom-default.xml"),
            metadataModuleDir.resolve("kcp-spread-pack-annotations-$version.pom"),
            StandardCopyOption.REPLACE_EXISTING,
        )
        Files.copy(
            annotationsBuildDir.resolve("libs/kcp-spread-pack-annotations-metadata-$version.jar"),
            metadataModuleDir.resolve("kcp-spread-pack-annotations-$version.jar"),
            StandardCopyOption.REPLACE_EXISTING,
        )

        val jvmPublicationDir = annotationsBuildDir.resolve("publications/jvm")
        val jvmModuleDir = repositoryDir
            .resolve(groupId.replace('.', '/'))
            .resolve("kcp-spread-pack-annotations-jvm")
            .resolve(version)
            .createDirectories()
        Files.copy(
            jvmPublicationDir.resolve("module.json"),
            jvmModuleDir.resolve("kcp-spread-pack-annotations-jvm-$version.module"),
            StandardCopyOption.REPLACE_EXISTING,
        )
        Files.copy(
            jvmPublicationDir.resolve("pom-default.xml"),
            jvmModuleDir.resolve("kcp-spread-pack-annotations-jvm-$version.pom"),
            StandardCopyOption.REPLACE_EXISTING,
        )
        Files.copy(
            annotationsBuildDir.resolve("libs/kcp-spread-pack-annotations-jvm-$version.jar"),
            jvmModuleDir.resolve("kcp-spread-pack-annotations-jvm-$version.jar"),
            StandardCopyOption.REPLACE_EXISTING,
        )
    }

    private fun inlineConsumerBuildFile(): String {
        val kotlinVersion = System.getProperty("spreadPack.kotlinVersion")
            ?: error("Missing spreadPack.kotlinVersion system property")
        return """
            plugins {
                kotlin("jvm") version ${kotlinVersion.quoteForKotlin()}
                id("site.addzero.kcp.spread-pack")
            }

            repositories {
                mavenCentral()
            }
        """.trimIndent()
    }

    private fun multiplatformConsumerBuildFile(): String {
        val kotlinVersion = System.getProperty("spreadPack.kotlinVersion")
            ?: error("Missing spreadPack.kotlinVersion system property")
        return """
            plugins {
                kotlin("multiplatform") version ${kotlinVersion.quoteForKotlin()}
                id("site.addzero.kcp.spread-pack")
            }

            kotlin {
                jvm()
                @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
                wasmJs {
                    browser()
                }
            }
        """.trimIndent()
    }

    private fun composeCarrierConsumerBuildFile(): String {
        val kotlinVersion = System.getProperty("spreadPack.kotlinVersion")
            ?: error("Missing spreadPack.kotlinVersion system property")
        return """
            plugins {
                kotlin("multiplatform") version ${kotlinVersion.quoteForKotlin()}
                id("site.addzero.kcp.spread-pack")
            }

            kotlin {
                jvm()
                @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
                wasmJs {
                    browser()
                }
                sourceSets {
                    commonMain {
                        dependencies {
                            implementation("org.jetbrains.compose.material3:material3:1.9.0")
                        }
                    }
                }
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
        val file = projectDir.resolve(relativePath)
        file.parent.createDirectories()
        file.writeText(content)
    }

    private fun copyCommittedExampleSources(projectDir: Path) {
        val repoRoot = System.getProperty("spreadPack.repoRoot")
            ?.let(Paths::get)
            ?: error("Missing spreadPack.repoRoot system property")
        val exampleDir = repoRoot
            .resolve("example/example-spread-pack")
            .normalize()
        require(Files.isDirectory(exampleDir)) {
            "Missing committed example project: $exampleDir"
        }
        val sourceDir = exampleDir.resolve("src")
        require(Files.isDirectory(sourceDir)) {
            "Missing committed example sources: $sourceDir"
        }
        copyDirectory(sourceDir, projectDir.resolve("src"))
    }

    private fun copyCommittedBuildScript(projectDir: Path) {
        val repoRoot = System.getProperty("spreadPack.repoRoot")
            ?.let(Paths::get)
            ?: error("Missing spreadPack.repoRoot system property")
        val buildFile = repoRoot
            .resolve("example/example-spread-pack/build.gradle.kts")
            .normalize()
        require(Files.isRegularFile(buildFile)) {
            "Missing committed example build script: $buildFile"
        }
        Files.copy(
            buildFile,
            projectDir.resolve("build.gradle.kts"),
            StandardCopyOption.REPLACE_EXISTING,
        )
    }

    private fun copyDirectory(sourceDir: Path, targetDir: Path) {
        Files.walk(sourceDir).use { paths ->
            paths.forEach { sourcePath ->
                val relativePath = sourceDir.relativize(sourcePath)
                val targetPath = targetDir.resolve(relativePath.toString())
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath)
                } else {
                    Files.createDirectories(targetPath.parent)
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
    }

    private fun createClassLoader(projectDir: Path): URLClassLoader {
        val kotlinClassesDir = projectDir.resolve("build/classes/kotlin/main")
        val kotlinStdlib = findClasspathEntry("kotlin-stdlib")
        val kotlinStdlibJdk8 = findClasspathEntry("kotlin-stdlib-jdk8")
        val urls = listOfNotNull(
            kotlinClassesDir.toUri().toURL(),
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

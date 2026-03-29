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
        val runtimeUtil = targetsKt.classLoader.loadClass("site.addzero.util.I8nutil")
        val runtimeUtilInstance = runtimeUtil.getField("INSTANCE").get(null)
        runtimeUtil.getMethod("setLocale", String::class.java).invoke(runtimeUtilInstance, "en")
        try {
            assertEquals("hello", targetsKt.getMethod("invoke").invoke(null))
        } finally {
            runtimeUtil.getMethod("clearLocale").invoke(runtimeUtilInstance)
        }
    }

    @Test
    fun sync_and_check_tasks_keep_locale_key_sets_aligned() {
        val javaHome = System.getProperty("java.home")
        val gradlePluginClasspath = System.getProperty("i18n.gradlePluginClasspath")
            ?: error("Missing i18n.gradlePluginClasspath system property")
        val localRepositoryDir = createLocalMavenRepository()
        val testProjectDir = Files.createTempDirectory("i18n-gradle-sync")
        writeFile(testProjectDir, "settings.gradle.kts", settingsFile(localRepositoryDir))
        writeFile(testProjectDir, "build.gradle.kts", buildFile(gradlePluginClasspath, managedLocales = listOf("en", "ja")))
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(
            testProjectDir,
            "src/main/kotlin/site/addzero/example/HelloTargets.kt",
            """
                package site.addzero.example

                fun helloMessage(): String = "你好"
            """.trimIndent(),
        )
        writeFile(
            testProjectDir,
            "src/main/kotlin/site/addzero/example/GoodbyeTargets.kt",
            """
                package site.addzero.example

                fun goodbyeMessage(): String = "再见"
            """.trimIndent(),
        )

        GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments("--stacktrace", "--console=plain", "syncI18nLocales")
            .forwardOutput()
            .build()

        val enFile = testProjectDir.resolve("src/main/resources/i18n/en.properties")
        val jaFile = testProjectDir.resolve("src/main/resources/i18n/ja.properties")
        assertTrue(Files.isRegularFile(enFile))
        assertTrue(Files.isRegularFile(jaFile))

        val enText = enFile.toFile().readText()
        val jaText = jaFile.toFile().readText()
        assertTrue(enText.contains("HelloTargets_helloMessage_text_你好="), enText)
        assertTrue(enText.contains("GoodbyeTargets_goodbyeMessage_text_再见="), enText)
        assertTrue(jaText.contains("HelloTargets_helloMessage_text_你好="), jaText)
        assertTrue(jaText.contains("GoodbyeTargets_goodbyeMessage_text_再见="), jaText)

        writeFile(
            testProjectDir,
            "src/main/kotlin/site/addzero/example/HelloTargets.kt",
            """
                package site.addzero.example

                fun helloMessage(): String = "你好"

                fun languageMessage(): String = "语言"
            """.trimIndent(),
        )

        GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments("--stacktrace", "--console=plain", "syncI18nLocales")
            .forwardOutput()
            .build()

        val resyncedEnText = enFile.toFile().readText()
        assertTrue(resyncedEnText.contains("HelloTargets_languageMessage_text_语言="), resyncedEnText)
        assertTrue(resyncedEnText.contains("GoodbyeTargets_goodbyeMessage_text_再见="), resyncedEnText)

        writeFile(
            testProjectDir,
            "src/main/resources/i18n/ja.properties",
            """
                HelloTargets_helloMessage_text_你好=こんにちは
            """.trimIndent(),
        )

        val failingResult = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments("--stacktrace", "--console=plain", "checkI18nLocales")
            .forwardOutput()
            .buildAndFail()

        assertTrue(failingResult.output.contains("Missing keys:"), failingResult.output)
        assertTrue(failingResult.output.contains("GoodbyeTargets_goodbyeMessage_text_再见"), failingResult.output)
    }

    @Test
    fun annotation_values_are_skipped_and_composable_only_scope_limits_rewrites() {
        val javaHome = System.getProperty("java.home")
        val gradlePluginClasspath = System.getProperty("i18n.gradlePluginClasspath")
            ?: error("Missing i18n.gradlePluginClasspath system property")
        val localRepositoryDir = createLocalMavenRepository()
        val testProjectDir = Files.createTempDirectory("i18n-gradle-compose-scope")
        writeFile(testProjectDir, "settings.gradle.kts", settingsFile(localRepositoryDir))
        writeFile(
            testProjectDir,
            "build.gradle.kts",
            buildFile(
                gradlePluginClasspath = gradlePluginClasspath,
                managedLocales = listOf("en"),
                scanScope = "composableOnly",
            ),
        )
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(
            testProjectDir,
            "src/main/kotlin/androidx/compose/runtime/Composable.kt",
            """
                package androidx.compose.runtime

                annotation class Composable
            """.trimIndent(),
        )
        writeFile(
            testProjectDir,
            "src/main/kotlin/site/addzero/example/RequestMapping.kt",
            """
                package site.addzero.example

                annotation class RequestMapping(val value: String)
            """.trimIndent(),
        )
        writeFile(
            testProjectDir,
            "src/main/kotlin/site/addzero/example/I18NTargets.kt",
            """
                package site.addzero.example

                import androidx.compose.runtime.Composable

                @RequestMapping("/api/demo")
                fun routePath(): String = "/api/demo"

                fun backendMessage(): String = "后端提示"

                @Composable
                fun uiMessage(): String = "界面文案"

                fun invokeRoute(): String = routePath()

                fun invokeBackend(): String = backendMessage()

                fun invokeUi(): String = uiMessage()
            """.trimIndent(),
        )
        writeFile(
            testProjectDir,
            "src/main/resources/i18n/en.properties",
            """
                I18NTargets_uiMessage_text_界面文案=ui text
            """.trimIndent(),
        )

        GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments("--stacktrace", "--console=plain", "syncI18nLocales", "build")
            .forwardOutput()
            .build()

        val enText = testProjectDir.resolve("src/main/resources/i18n/en.properties").toFile().readText()
        assertTrue(enText.contains("I18NTargets_uiMessage_text_界面文案="), enText)
        assertTrue(!enText.contains("/api/demo"), enText)
        assertTrue(!enText.contains("I18NTargets_backendMessage_text_后端提示="), enText)

        val targetsKt = loadClass(
            classesDir = testProjectDir.resolve("build/classes/kotlin/main"),
            resourcesDir = testProjectDir.resolve("build/resources/main"),
            name = "site.addzero.example.I18NTargetsKt",
        )
        val runtimeUtil = targetsKt.classLoader.loadClass("site.addzero.util.I8nutil")
        val runtimeUtilInstance = runtimeUtil.getField("INSTANCE").get(null)
        runtimeUtil.getMethod("setLocale", String::class.java).invoke(runtimeUtilInstance, "en")
        try {
            assertEquals("/api/demo", targetsKt.getMethod("invokeRoute").invoke(null))
            assertEquals("后端提示", targetsKt.getMethod("invokeBackend").invoke(null))
            assertEquals("ui text", targetsKt.getMethod("invokeUi").invoke(null))
        } finally {
            runtimeUtil.getMethod("clearLocale").invoke(runtimeUtilInstance)
        }
    }

    private fun settingsFile(localRepositoryDir: Path): String {
        return """
            import org.gradle.api.initialization.resolve.RepositoriesMode

            dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
                repositories {
                    maven(url = uri(${localRepositoryDir.toString().quoteForKotlin()}))
                    mavenCentral()
                    google()
                }
            }

            rootProject.name = "i18n-consumer"
        """.trimIndent()
    }

    private fun buildFile(
        gradlePluginClasspath: String,
        managedLocales: List<String> = emptyList(),
        scanScope: String? = null,
    ): String {
        val classpathEntries = gradlePluginClasspath
            .split(File.pathSeparator)
            .filter(String::isNotBlank)
            .joinToString(separator = ",\n                        ") { path ->
                path.quoteForKotlin()
            }
        val managedLocalesDsl = if (managedLocales.isEmpty()) {
            ""
        } else {
            """
                managedLocales.addAll(${managedLocales.joinToString(", ") { locale -> locale.quoteForKotlin() }})
            """.trimIndent()
        }
        val scanScopeDsl = scanScope?.let { value ->
            """scanScope.set(${value.quoteForKotlin()})"""
        }.orEmpty()
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
                $managedLocalesDsl
                $scanScopeDsl
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
        val repositoryDir = Files.createTempDirectory("i18n-m2")
        installModule(
            repositoryDir = repositoryDir,
            groupId = pluginGroup,
            artifactId = "kcp-i18n",
            version = pluginVersion,
            jarFile = findPrimaryJar(compilerPluginBuildDir, "kcp-i18n", pluginVersion),
        )
        installModule(
            repositoryDir = repositoryDir,
            groupId = pluginGroup,
            artifactId = "kcp-i18n-runtime",
            version = pluginVersion,
            jarFile = findPrimaryJar(runtimeBuildDir, "kcp-i18n-runtime", pluginVersion),
        )
        return repositoryDir
    }

    private fun findPrimaryJar(
        buildDir: Path,
        artifactPrefix: String,
        version: String? = null,
    ): Path {
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
            version?.let { currentVersion ->
                candidates.firstOrNull { path ->
                    path.fileName.toString() == "$artifactPrefix-$currentVersion.jar"
                }?.let { return it }
            }
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
        val pluginVersion = System.getProperty("i18n.pluginVersion")
            ?: error("Missing i18n.pluginVersion system property")
        val runtimeJar = findPrimaryJar(runtimeBuildDir, "kcp-i18n-runtime", pluginVersion)
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

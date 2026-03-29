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
    fun annotation_values_are_skipped_by_default_and_composable_only_scope_limits_rewrites() {
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

    @Test
    fun builtin_route_annotation_values_are_catalog_only_and_can_translate_by_source() {
        val javaHome = System.getProperty("java.home")
        val gradlePluginClasspath = System.getProperty("i18n.gradlePluginClasspath")
            ?: error("Missing i18n.gradlePluginClasspath system property")
        val localRepositoryDir = createLocalMavenRepository()
        val testProjectDir = Files.createTempDirectory("i18n-gradle-annotation-whitelist")
        writeFile(testProjectDir, "settings.gradle.kts", settingsFile(localRepositoryDir))
        writeFile(
            testProjectDir,
            "build.gradle.kts",
            buildFile(
                gradlePluginClasspath = gradlePluginClasspath,
                managedLocales = listOf("en"),
            ),
        )
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(
            testProjectDir,
            "src/main/kotlin/site/addzero/example/Route.kt",
            """
                package site.addzero.example

                @Retention(AnnotationRetention.RUNTIME)
                annotation class Route(val label: String)
            """.trimIndent(),
        )
        writeFile(
            testProjectDir,
            "src/main/kotlin/site/addzero/example/I18NTargets.kt",
            """
                package site.addzero.example

                @Route("用户管理")
                fun screenRoute() {
                }
            """.trimIndent(),
        )

        GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments("--stacktrace", "--console=plain", "syncI18nLocales")
            .forwardOutput()
            .build()

        val enFile = testProjectDir.resolve("src/main/resources/i18n/en.properties").toFile()
        val generatedTranslations = enFile.readText()
        assertTrue(generatedTranslations.contains("Route_text_用户管理="), generatedTranslations)
        val routeKey = generatedTranslations
            .lineSequence()
            .firstOrNull { line -> line.contains("Route_text_用户管理=") }
            ?.substringBefore('=')
            ?: error("Missing Route annotation key in generated translations")
        enFile.writeText(
            generatedTranslations.replace("$routeKey=", "$routeKey=User Management"),
        )

        GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments("--stacktrace", "--console=plain", "build")
            .forwardOutput()
            .build()

        val catalogResource = testProjectDir.resolve("build/resources/main/i18n/_catalog.properties").toFile()
        assertTrue(catalogResource.isFile, "Missing runtime catalog resource: ${catalogResource.absolutePath}")
        assertTrue(catalogResource.readText().contains("$routeKey=用户管理"), catalogResource.readText())

        val targetsKt = loadClass(
            classesDir = testProjectDir.resolve("build/classes/kotlin/main"),
            resourcesDir = testProjectDir.resolve("build/resources/main"),
            name = "site.addzero.example.I18NTargetsKt",
        )
        @Suppress("UNCHECKED_CAST")
        val routeAnnotationClass = targetsKt.classLoader.loadClass("site.addzero.example.Route") as Class<out Annotation>
        val routeAnnotation = targetsKt.getMethod("screenRoute").getAnnotation(routeAnnotationClass)
        assertEquals("用户管理", routeAnnotationClass.getMethod("label").invoke(routeAnnotation))

        val runtimeUtil = targetsKt.classLoader.loadClass("site.addzero.util.I8nutil")
        val runtimeUtilInstance = runtimeUtil.getField("INSTANCE").get(null)
        runtimeUtil.getMethod("setLocale", String::class.java).invoke(runtimeUtilInstance, "en")
        try {
            assertEquals(
                "User Management",
                runtimeUtil.getMethod("tBySource", String::class.java, String::class.java)
                    .invoke(runtimeUtilInstance, "用户管理", "i18n"),
            )
        } finally {
            runtimeUtil.getMethod("clearLocale").invoke(runtimeUtilInstance)
        }
    }

    @Test
    fun custom_annotation_whitelist_extends_builtin_rules() {
        val javaHome = System.getProperty("java.home")
        val gradlePluginClasspath = System.getProperty("i18n.gradlePluginClasspath")
            ?: error("Missing i18n.gradlePluginClasspath system property")
        val localRepositoryDir = createLocalMavenRepository()
        val testProjectDir = Files.createTempDirectory("i18n-gradle-custom-annotation-whitelist")
        writeFile(testProjectDir, "settings.gradle.kts", settingsFile(localRepositoryDir))
        writeFile(
            testProjectDir,
            "build.gradle.kts",
            buildFile(
                gradlePluginClasspath = gradlePluginClasspath,
                managedLocales = listOf("en"),
                annotationWhitelist = listOf("ScreenLabel"),
            ),
        )
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(
            testProjectDir,
            "src/main/kotlin/site/addzero/example/ScreenLabel.kt",
            """
                package site.addzero.example

                @Retention(AnnotationRetention.RUNTIME)
                annotation class ScreenLabel(val text: String)
            """.trimIndent(),
        )
        writeFile(
            testProjectDir,
            "src/main/kotlin/site/addzero/example/I18NTargets.kt",
            """
                package site.addzero.example

                @ScreenLabel("设备中心")
                fun deviceScreen() {
                }
            """.trimIndent(),
        )

        GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments("--stacktrace", "--console=plain", "syncI18nLocales")
            .forwardOutput()
            .build()

        val enFile = testProjectDir.resolve("src/main/resources/i18n/en.properties").toFile()
        val generatedTranslations = enFile.readText()
        assertTrue(generatedTranslations.contains("ScreenLabel_text_设备中心="), generatedTranslations)
    }

    @Test
    fun annotation_blacklist_overrides_builtin_whitelist() {
        val javaHome = System.getProperty("java.home")
        val gradlePluginClasspath = System.getProperty("i18n.gradlePluginClasspath")
            ?: error("Missing i18n.gradlePluginClasspath system property")
        val localRepositoryDir = createLocalMavenRepository()
        val testProjectDir = Files.createTempDirectory("i18n-gradle-annotation-blacklist")
        writeFile(testProjectDir, "settings.gradle.kts", settingsFile(localRepositoryDir))
        writeFile(
            testProjectDir,
            "build.gradle.kts",
            buildFile(
                gradlePluginClasspath = gradlePluginClasspath,
                managedLocales = listOf("en"),
                annotationBlacklist = listOf("site.addzero.example.Route"),
            ),
        )
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(
            testProjectDir,
            "src/main/kotlin/site/addzero/example/Route.kt",
            """
                package site.addzero.example

                @Retention(AnnotationRetention.RUNTIME)
                annotation class Route(val label: String)
            """.trimIndent(),
        )
        writeFile(
            testProjectDir,
            "src/main/kotlin/site/addzero/example/I18NTargets.kt",
            """
                package site.addzero.example

                @Route("用户管理")
                fun screenRoute() {
                }

                fun helloMessage(): String = "你好"
            """.trimIndent(),
        )

        GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments("--stacktrace", "--console=plain", "syncI18nLocales", "build")
            .forwardOutput()
            .build()

        val enText = testProjectDir.resolve("src/main/resources/i18n/en.properties").toFile().readText()
        assertTrue(enText.contains("I18NTargets_helloMessage_text_你好="), enText)
        assertTrue(!enText.contains("Route_text_用户管理="), enText)

        val targetsKt = loadClass(
            classesDir = testProjectDir.resolve("build/classes/kotlin/main"),
            resourcesDir = testProjectDir.resolve("build/resources/main"),
            name = "site.addzero.example.I18NTargetsKt",
        )
        val runtimeUtil = targetsKt.classLoader.loadClass("site.addzero.util.I8nutil")
        val runtimeUtilInstance = runtimeUtil.getField("INSTANCE").get(null)
        runtimeUtil.getMethod("setLocale", String::class.java).invoke(runtimeUtilInstance, "en")
        try {
            assertEquals(
                "用户管理",
                runtimeUtil.getMethod("tBySource", String::class.java, String::class.java)
                    .invoke(runtimeUtilInstance, "用户管理", "i18n"),
            )
        } finally {
            runtimeUtil.getMethod("clearLocale").invoke(runtimeUtilInstance)
        }
    }

    @Test
    fun builtin_annotation_rules_can_be_disabled() {
        val javaHome = System.getProperty("java.home")
        val gradlePluginClasspath = System.getProperty("i18n.gradlePluginClasspath")
            ?: error("Missing i18n.gradlePluginClasspath system property")
        val localRepositoryDir = createLocalMavenRepository()
        val testProjectDir = Files.createTempDirectory("i18n-gradle-disable-builtin-annotation-rules")
        writeFile(testProjectDir, "settings.gradle.kts", settingsFile(localRepositoryDir))
        writeFile(
            testProjectDir,
            "build.gradle.kts",
            buildFile(
                gradlePluginClasspath = gradlePluginClasspath,
                managedLocales = listOf("en"),
                useDefaultAnnotationRules = false,
            ),
        )
        writeFile(testProjectDir, "gradle.properties", gradleProperties(javaHome))
        writeFile(
            testProjectDir,
            "src/main/kotlin/site/addzero/example/Route.kt",
            """
                package site.addzero.example

                @Retention(AnnotationRetention.RUNTIME)
                annotation class Route(val label: String)
            """.trimIndent(),
        )
        writeFile(
            testProjectDir,
            "src/main/kotlin/site/addzero/example/I18NTargets.kt",
            """
                package site.addzero.example

                @Route("用户管理")
                fun screenRoute() {
                }

                fun helloMessage(): String = "你好"
            """.trimIndent(),
        )

        GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withEnvironment(mapOf("JAVA_HOME" to javaHome))
            .withArguments("--stacktrace", "--console=plain", "syncI18nLocales", "build")
            .forwardOutput()
            .build()

        val enText = testProjectDir.resolve("src/main/resources/i18n/en.properties").toFile().readText()
        assertTrue(enText.contains("I18NTargets_helloMessage_text_你好="), enText)
        assertTrue(!enText.contains("Route_text_用户管理="), enText)
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
        useDefaultAnnotationRules: Boolean? = null,
        annotationWhitelist: List<String> = emptyList(),
        annotationBlacklist: List<String> = emptyList(),
    ): String {
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

            val i18nExtension = extensions.getByName("i18n")

            fun isCompatibleParameter(parameterType: Class<*>, value: Any): Boolean {
                if (parameterType.isAssignableFrom(value.javaClass)) {
                    return true
                }
                if (!parameterType.isPrimitive) {
                    return false
                }
                return when (parameterType) {
                    java.lang.Boolean.TYPE -> value is Boolean
                    java.lang.Integer.TYPE -> value is Int
                    java.lang.Long.TYPE -> value is Long
                    java.lang.Double.TYPE -> value is Double
                    java.lang.Float.TYPE -> value is Float
                    else -> false
                }
            }

            fun findCompatibleSingleArgMethod(target: Any, methodName: String, value: Any): java.lang.reflect.Method {
                return target.javaClass.methods.first { method ->
                    method.name == methodName &&
                        method.parameterCount == 1 &&
                        isCompatibleParameter(method.parameterTypes[0], value)
                }
            }

            fun setI18nProperty(getterName: String, value: Any) {
                val property = i18nExtension.javaClass.getMethod(getterName).invoke(i18nExtension)
                val setter = findCompatibleSingleArgMethod(property, "set", value)
                setter.invoke(property, value)
            }

            fun addI18nListPropertyValue(getterName: String, value: String) {
                val property = i18nExtension.javaClass.getMethod(getterName).invoke(i18nExtension)
                val addMethod = findCompatibleSingleArgMethod(property, "add", value)
                addMethod.invoke(property, value)
            }

            setI18nProperty("getTargetLocale", "en")
            setI18nProperty("getResourceBasePath", "i18n")
            ${managedLocales.joinToString("\n") { locale ->
                "addI18nListPropertyValue(\"getManagedLocales\", ${locale.quoteForKotlin()})"
            }}
            ${scanScope?.let { value ->
                "setI18nProperty(\"getScanScope\", ${value.quoteForKotlin()})"
            }.orEmpty()}
            ${useDefaultAnnotationRules?.let { value ->
                "setI18nProperty(\"getUseDefaultAnnotationRules\", $value)"
            }.orEmpty()}
            ${annotationWhitelist.joinToString("\n") { value ->
                "addI18nListPropertyValue(\"getAnnotationWhitelist\", ${value.quoteForKotlin()})"
            }}
            ${annotationBlacklist.joinToString("\n") { value ->
                "addI18nListPropertyValue(\"getAnnotationBlacklist\", ${value.quoteForKotlin()})"
            }}
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

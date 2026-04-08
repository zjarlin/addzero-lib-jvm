package site.addzero.ksp.gradle.test

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertTrue
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test

class PublishedKspPluginSmokeTest {

    private val runtimeClasspath: String = System.getProperty("publishedKsp.testRuntimeClasspath")
        ?: error("Missing publishedKsp.testRuntimeClasspath system property")

    @Test
    fun `jvm options spring2ktor plugin injects companions and serializes args`() {
        val output = runBuild(
            projectName = "spring2ktor-consumer",
            buildScript = jvmPluginBuildScript(
                pluginId = "site.addzero.ksp.spring2ktor-server",
                serializedArgsKey = "site.addzero.kspconsumer.site.addzero.ksp.spring2ktor-server.serializedArgs",
                extraBody = """
                    configureDynamicExtension(
                        "spring2ktorServer",
                        mapOf("generatedPackage" to "demo.generated.springktor"),
                    )
                """.trimIndent(),
            ),
        )

        assertContains(
            output,
            "CONF[ksp]=site.addzero:spring2ktor-server-processor",
            "CONF[implementation]=site.addzero:spring2ktor-server-core",
            "CONF[compileOnly]=org.springframework:spring-web",
            "springKtor.generatedPackage=demo.generated.springktor",
        )
    }

    @Test
    fun `kmp options route plugin injects route core and must-map args`() {
        val output = runBuild(
            projectName = "route-consumer",
            buildScript = kmpPluginBuildScript(
                pluginId = "site.addzero.ksp.route",
                serializedArgsKey = "site.addzero.kspconsumer.site.addzero.ksp.route.serializedArgs",
                extraBody = """
                    configureDynamicExtension(
                        "route",
                        mapOf(
                            "generatedPackage" to "demo.generated.route",
                            "routeOwnerModule" to layout.projectDirectory.dir("src/commonMain/kotlin").asFile.absolutePath,
                            "aggregationRole" to "owner",
                            "moduleKey" to "feature-route",
                        ),
                    )
                """.trimIndent(),
            ),
        )

        assertContains(
            output,
            "CONF[kspCommonMainMetadata]=site.addzero:route-processor",
            "CONF[commonMainImplementation]=site.addzero:route-core",
            "routeGenPkg=demo.generated.route",
            "routeAggregationRole=owner",
            "routeModuleKey=feature-route",
            "TASK[compileKotlinJvm]=",
            "kspKotlinJvm",
        )
    }

    @Test
    fun `kmp options modbus rtu plugin injects runtime companion`() {
        val output = runBuild(
            projectName = "modbus-rtu-consumer",
            buildScript = kmpPluginBuildScript(
                pluginId = "site.addzero.ksp.modbus-rtu",
                serializedArgsKey = "site.addzero.kspconsumer.site.addzero.ksp.modbus-rtu.serializedArgs",
                extraBody = """
                    configureDynamicExtension(
                        "modbusRtu",
                        mapOf(
                            "codegenModes" to listOf("server", "contract"),
                            "contractPackages" to listOf("site.addzero.device.contract"),
                            "transports" to listOf("rtu", "tcp"),
                            "cOutputProjectDir" to "/tmp/firmware-project",
                            "bridgeImplPath" to "Core/Src/modbus",
                            "keilUvprojxPath" to "MDK-ARM/test1.uvprojx",
                            "keilTargetName" to "test1",
                            "keilGroupName" to "Core/modbus/rtu",
                            "mxprojectPath" to ".mxproject",
                            "springRouteOutputDir" to "/tmp/generated-spring-routes",
                        ),
                    )
                """.trimIndent(),
            ),
        )

        assertContains(
            output,
            "CONF[kspJvm]=site.addzero:modbus-ksp-rtu",
            "CONF[commonMainImplementation]=site.addzero:modbus-runtime",
            "addzero.modbus.codegen.mode=server,contract",
            "addzero.modbus.transports=rtu,tcp",
            "addzero.modbus.contractPackages=site.addzero.device.contract",
            "addzero.modbus.c.output.projectDir=/tmp/firmware-project",
            "addzero.modbus.spring.route.outputDir=/tmp/generated-spring-routes",
        )
    }

    @Test
    fun `kmp umbrella plugin adds direct and spi subprocessors`() {
        val output = runBuild(
            projectName = "jimmer-external-consumer",
            buildScript = kmpPluginBuildScript(
                pluginId = "site.addzero.ksp.jimmer-entity-external",
                serializedArgsKey =
                    "site.addzero.kspconsumer.site.addzero.ksp.jimmer-entity-external.serializedArgs",
                extraBody = """
                    configureDynamicExtension(
                        "jimmerEntityExternal",
                        mapOf("apiClientPackageName" to "demo.generated.api"),
                    )
                    configureNestedDynamicExtension(
                        "jimmerEntityExternal",
                        "entity2Iso",
                        mapOf("packageName" to "demo.generated.isomorphic"),
                    )
                    configureNestedDynamicExtension(
                        "jimmerEntityExternal",
                        "entity2Form",
                        mapOf("packageName" to "demo.generated.forms"),
                    )
                    configureNestedDynamicExtension(
                        "jimmerEntityExternal",
                        "entity2Mcp",
                        mapOf("packageName" to "demo.generated.mcp"),
                    )
                """.trimIndent(),
            ),
        )

        assertContains(
            output,
            "CONF[kspCommonMainMetadata]=site.addzero:jimmer-entity-external-processor|" +
                "site.addzero:entity2iso-processor|" +
                "site.addzero:entity2form-processor|" +
                "site.addzero:entity2mcp-processor",
            "entity2Iso.enabled=true",
            "isomorphicSerializableEnabled=true",
            "formPackageName=demo.generated.forms",
            "mcpPackageName=demo.generated.mcp",
        )
    }

    @Test
    fun `kmp zero config gen reified plugin injects core companion`() {
        val output = runBuild(
            projectName = "gen-reified-consumer",
            buildScript = kmpPluginBuildScript(
                pluginId = "site.addzero.ksp.gen-reified",
                serializedArgsKey = "site.addzero.kspconsumer.site.addzero.ksp.gen-reified.serializedArgs",
            ),
        )

        assertContains(
            output,
            "CONF[kspCommonMainMetadata]=site.addzero:gen-reified-processor",
            "CONF[commonMainImplementation]=site.addzero:gen-reified-core",
            "ARGS={}",
        )
    }

    @Test
    fun `kmp zero config ksp dsl builder plugin injects core companion`() {
        val output = runBuild(
            projectName = "ksp-dsl-builder-consumer",
            buildScript = kmpPluginBuildScript(
                pluginId = "site.addzero.ksp.ksp-dsl-builder",
                serializedArgsKey =
                    "site.addzero.kspconsumer.site.addzero.ksp.ksp-dsl-builder.serializedArgs",
            ),
        )

        assertContains(
            output,
            "CONF[kspCommonMainMetadata]=site.addzero:ksp-dsl-builder-processor",
            "CONF[commonMainImplementation]=site.addzero:ksp-dsl-builder-core",
            "ARGS={}",
        )
    }

    @Test
    fun `jvm zero config multireceiver plugin injects annotations companion`() {
        val output = runBuild(
            projectName = "multireceiver-consumer",
            buildScript = jvmPluginBuildScript(
                pluginId = "site.addzero.ksp.multireceiver",
                serializedArgsKey = "site.addzero.kspconsumer.site.addzero.ksp.multireceiver.serializedArgs",
            ),
        )

        assertContains(
            output,
            "CONF[ksp]=site.addzero:multireceiver-processor",
            "CONF[implementation]=site.addzero:kcp-multireceiver-annotations",
            "ARGS={}",
        )
    }

    @Test
    fun `raw kmp jdbc2entity consumer remains available without plugin`() {
        val output = runBuild(
            projectName = "jdbc2entity-raw-consumer",
            buildScript = rawKmpBuildScript(
                dependencyNotation = "\"site.addzero:jdbc2entity-processor:2026.04.07\"",
                rawArgs = emptyMap(),
                extraBody = "",
            ),
        )

        assertContains(
            output,
            "CONF[kspCommonMainMetadata]=site.addzero:jdbc2entity-processor",
            "CONF[commonMainImplementation]=",
            "SRC[commonMain]=build/generated/ksp/metadata/commonMain/kotlin",
            "ARGS={}",
        )
    }

    @Test
    fun `raw kmp controller2api consumer remains available without plugin and supports direct args`() {
        val output = runBuild(
            projectName = "controller2api-raw-consumer",
            buildScript = rawKmpBuildScript(
                dependencyNotation = "\"site.addzero:controller2api-processor:2026.04.07\"",
                rawArgs = linkedMapOf(
                    "apiClientPackageName" to "demo.generated.api",
                    "apiClientAggregatorObjectName" to "Apis",
                    "apiClientAggregatorStyle" to "koin",
                    "apiClientAggregatorOutputDir" to "/tmp/generated/apis",
                    "apiClientOutputDir" to "/tmp/generated/clients",
                ),
                extraBody = "",
            ),
        )

        assertContains(
            output,
            "CONF[kspCommonMainMetadata]=site.addzero:controller2api-processor",
            "apiClientPackageName=demo.generated.api",
            "apiClientAggregatorObjectName=Apis",
            "apiClientAggregatorStyle=koin",
            "apiClientAggregatorOutputDir=/tmp/generated/apis",
            "apiClientOutputDir=/tmp/generated/clients",
        )
    }

    private fun jvmPluginBuildScript(
        pluginId: String,
        serializedArgsKey: String,
        extraBody: String = "",
    ): String {
        val pluginIdLiteral = pluginId.quoteForKotlin()
        val quotedArgsKey = serializedArgsKey.quoteForKotlin()
        val kspConfig = "ksp".quoteForKotlin()
        val implementationConfig = "implementation".quoteForKotlin()
        val compileOnlyConfig = "compileOnly".quoteForKotlin()
        val compileTaskName = "compileKotlin".quoteForKotlin()
        return """
            ${buildscriptBlock()}
            ${dynamicExtensionHelpers()}

            apply<org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper>()
            apply(plugin = $pluginIdLiteral)

            $extraBody

            tasks.register("verifyPublishedKspPlugin") {
                doLast {
                    println("CONF[ksp]=${'$'}{dumpConfiguration($kspConfig)}")
                    println("CONF[implementation]=${'$'}{dumpConfiguration($implementationConfig)}")
                    println("CONF[compileOnly]=${'$'}{dumpConfiguration($compileOnlyConfig)}")
                    println("SRC[jvm]=${'$'}{dumpJvmSourceDirs()}")
                    println("TASK[compileKotlin]=${'$'}{dumpCompileTaskDependencies($compileTaskName)}")
                    println("ARGS=${'$'}{project.extensions.extraProperties.get($quotedArgsKey)}")
                }
            }
        """.trimIndent()
    }

    private fun kmpPluginBuildScript(
        pluginId: String,
        serializedArgsKey: String,
        extraBody: String = "",
    ): String {
        val pluginIdLiteral = pluginId.quoteForKotlin()
        val quotedArgsKey = serializedArgsKey.quoteForKotlin()
        val kspCommonMainMetadataConfig = "kspCommonMainMetadata".quoteForKotlin()
        val kspJvmConfig = "kspJvm".quoteForKotlin()
        val commonMainImplementationConfig = "commonMainImplementation".quoteForKotlin()
        val jvmMainImplementationConfig = "jvmMainImplementation".quoteForKotlin()
        val compileTaskName = "compileKotlinJvm".quoteForKotlin()
        return """
            ${buildscriptBlock()}
            ${dynamicExtensionHelpers()}

            apply<org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper>()
            extensions.configure(org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java) {
                jvm()
            }
            apply(plugin = $pluginIdLiteral)

            $extraBody

            tasks.register("verifyPublishedKspPlugin") {
                doLast {
                    println("CONF[kspCommonMainMetadata]=${'$'}{dumpConfiguration($kspCommonMainMetadataConfig)}")
                    println("CONF[kspJvm]=${'$'}{dumpConfiguration($kspJvmConfig)}")
                    println("CONF[commonMainImplementation]=${'$'}{dumpConfiguration($commonMainImplementationConfig)}")
                    println("CONF[jvmMainImplementation]=${'$'}{dumpConfiguration($jvmMainImplementationConfig)}")
                    println("SRC[commonMain]=${'$'}{dumpKmpSourceDirs(${"commonMain".quoteForKotlin()})}")
                    println("SRC[jvmMain]=${'$'}{dumpKmpSourceDirs(${"jvmMain".quoteForKotlin()})}")
                    println("TASK[compileKotlinJvm]=${'$'}{dumpCompileTaskDependencies($compileTaskName)}")
                    println("ARGS=${'$'}{project.extensions.extraProperties.get($quotedArgsKey)}")
                }
            }
        """.trimIndent()
    }

    private fun rawKmpBuildScript(
        dependencyNotation: String,
        rawArgs: Map<String, String>,
        extraBody: String,
    ): String {
        val rawArgsLiteral = if (rawArgs.isEmpty()) {
            "emptyMap<String, String>()"
        } else {
            rawArgs.entries.joinToString(
                separator = ",\n                    ",
                prefix = "linkedMapOf<String, String>(\n                    ",
                postfix = "\n                )",
            ) { (key, value) ->
                "${key.quoteForKotlin()} to ${value.quoteForKotlin()}"
            }
        }
        val kspCommonMainMetadataConfig = "kspCommonMainMetadata".quoteForKotlin()
        val commonMainImplementationConfig = "commonMainImplementation".quoteForKotlin()
        val commonMainSourceSet = "commonMain".quoteForKotlin()
        val jvmMainSourceSet = "jvmMain".quoteForKotlin()
        return """
            ${buildscriptBlock()}
            ${dynamicExtensionHelpers()}

            apply<org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper>()
            extensions.configure(org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java) {
                jvm()
                sourceSets.getByName("commonMain").kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
                sourceSets.getByName("jvmMain").kotlin.srcDir("build/generated/ksp/jvm/jvmMain/kotlin")
            }
            apply(plugin = "com.google.devtools.ksp")

            dependencies {
                add("kspCommonMainMetadata", $dependencyNotation)
            }

            val rawArgs =
                $rawArgsLiteral

            (extensions.getByName("ksp") as com.google.devtools.ksp.gradle.KspExtension).apply {
                rawArgs.forEach { (key, value) -> arg(key, value) }
            }

            $extraBody

            tasks.register("verifyPublishedKspPlugin") {
                doLast {
                    println("CONF[kspCommonMainMetadata]=${'$'}{dumpConfiguration($kspCommonMainMetadataConfig)}")
                    println("CONF[commonMainImplementation]=${'$'}{dumpConfiguration($commonMainImplementationConfig)}")
                    println("SRC[commonMain]=${'$'}{dumpKmpSourceDirs($commonMainSourceSet)}")
                    println("SRC[jvmMain]=${'$'}{dumpKmpSourceDirs($jvmMainSourceSet)}")
                    println("ARGS=${'$'}rawArgs")
                }
            }
        """.trimIndent()
    }

    private fun dynamicExtensionHelpers(): String = """
        import org.gradle.api.Project
        import org.gradle.api.provider.ListProperty
        import org.gradle.api.provider.Property

        @Suppress("UNCHECKED_CAST")
        fun Any.dynamicMember(name: String): Any {
            val getterName = "get" + name.replaceFirstChar(Char::uppercase)
            return javaClass.methods
                .first { it.name == getterName && it.parameterCount == 0 }
                .invoke(this)
        }

        @Suppress("UNCHECKED_CAST")
        fun Any.setDynamicValue(name: String, value: Any) {
            when (val holder = dynamicMember(name)) {
                is Property<*> -> (holder as Property<Any>).set(value)
                is ListProperty<*> -> (holder as ListProperty<Any>).set(value as List<Any>)
                else -> error("Unsupported dynamic property " + name + " on " + javaClass.name)
            }
        }

        fun Project.configureDynamicExtension(name: String, values: Map<String, Any>) {
            val extension = extensions.getByName(name)
            values.forEach { (key, value) -> extension.setDynamicValue(key, value) }
        }

        fun Project.configureNestedDynamicExtension(
            name: String,
            nested: String,
            values: Map<String, Any>,
        ) {
            val extension = extensions.getByName(name).dynamicMember(nested)
            values.forEach { (key, value) -> extension.setDynamicValue(key, value) }
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
                .map { it.relativeTo(projectDir).path }
                .sorted()
                .joinToString("|")

        fun Project.dumpKmpSourceDirs(name: String): String =
            extensions.getByType(org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java)
                .sourceSets
                .getByName(name)
                .kotlin
                .srcDirs
                .map { it.relativeTo(projectDir).path }
                .sorted()
                .joinToString("|")

        fun Project.dumpCompileTaskDependencies(name: String): String {
            val task = tasks.getByName(name)
            return task.taskDependencies.getDependencies(task)
                .map { it.name }
                .sorted()
                .joinToString("|")
        }
    """.trimIndent()

    private fun buildscriptBlock(): String {
        val classpathEntries = runtimeClasspath
            .split(File.pathSeparator)
            .filter(String::isNotBlank)
            .joinToString(separator = ",\n                        ") { entry ->
                entry.quoteForKotlin()
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
        """.trimIndent()
    }

    private fun runBuild(
        projectName: String,
        buildScript: String,
    ): String {
        val projectDir = Files.createTempDirectory(projectName)
        writeFile(projectDir, "settings.gradle.kts", """rootProject.name = "$projectName"""")
        writeFile(
            projectDir,
            "gradle.properties",
            """
                org.gradle.parallel=false
                org.gradle.configuration-cache=false
            """.trimIndent(),
        )
        writeFile(projectDir, "build.gradle.kts", buildScript)

        val result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments(
                "--stacktrace",
                "--console=plain",
                "verifyPublishedKspPlugin",
            )
            .build()
        return result.output
    }

    private fun writeFile(
        projectDir: Path,
        relativePath: String,
        content: String,
    ) {
        val file = projectDir.resolve(relativePath)
        Files.createDirectories(file.parent)
        Files.writeString(file, content)
    }

    private fun assertContains(
        output: String,
        vararg snippets: String,
    ) {
        snippets.forEach { snippet ->
            assertTrue(output.contains(snippet), "Expected output to contain <$snippet> but was:\n$output")
        }
    }
}

private fun String.quoteForKotlin(): String =
    buildString(length + 2) {
        append('"')
        for (char in this@quoteForKotlin) {
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                else -> append(char)
            }
        }
        append('"')
    }

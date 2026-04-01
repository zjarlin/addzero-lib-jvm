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
    fun `jvm no-options logger plugin wires ksp and generated sources`() {
        val output = runBuild(
            projectName = "logger-consumer",
            buildScript = jvmBuildScript(
                pluginClass = "site.addzero.ksp.logger.gradle.LoggerGradlePlugin",
                serializedArgsKey = "site.addzero.kspconsumer.site.addzero.ksp.logger.serializedArgs",
                extraBody = "",
            ),
        )

        assertContains(
            output,
            "CONF[ksp]=site.addzero:logger-processor",
            "build/generated/ksp/main/kotlin",
            "TASK[compileKotlin]=",
            "kspKotlin",
            "ARGS={}",
        )
    }

    @Test
    fun `jvm options spring2ktor plugin injects companions and serializes args`() {
        val output = runBuild(
            projectName = "spring2ktor-consumer",
            buildScript = jvmBuildScript(
                pluginClass = "site.addzero.ksp.spring2ktorserver.gradle.Spring2KtorServerGradlePlugin",
                serializedArgsKey = "site.addzero.kspconsumer.site.addzero.ksp.spring2ktor-server.serializedArgs",
                extraBody = """
                    extensions.configure(site.addzero.ksp.spring2ktorserver.gradle.Spring2KtorServerExtension::class.java) {
                        generatedPackage.set("demo.generated.springktor")
                    }
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
    fun `jvm options modbus rtu plugin injects runtime companion`() {
        val output = runBuild(
            projectName = "modbus-rtu-consumer",
            buildScript = jvmBuildScript(
                pluginClass = "site.addzero.ksp.modbusrtu.gradle.ModbusRtuGradlePlugin",
                serializedArgsKey = "site.addzero.kspconsumer.site.addzero.ksp.modbus-rtu.serializedArgs",
                extraBody = """
                    extensions.configure(site.addzero.ksp.modbusrtu.gradle.ModbusRtuExtension::class.java) {
                        codegenModes.set(listOf("server", "contract"))
                        contractPackages.set(listOf("site.addzero.device.contract"))
                        transports.set(listOf("rtu", "tcp"))
                    }
                """.trimIndent(),
            ),
        )

        assertContains(
            output,
            "CONF[ksp]=site.addzero:modbus-ksp-rtu",
            "CONF[implementation]=site.addzero:modbus-runtime",
            "addzero.modbus.codegen.mode=server,contract",
            "addzero.modbus.transports=rtu,tcp",
            "addzero.modbus.contractPackages=site.addzero.device.contract",
        )
    }

    @Test
    fun `kmp options route plugin injects route core and must-map args`() {
        val output = runBuild(
            projectName = "route-consumer",
            buildScript = kmpBuildScript(
                pluginClass = "site.addzero.ksp.route.gradle.RouteGradlePlugin",
                serializedArgsKey = "site.addzero.kspconsumer.site.addzero.ksp.route.serializedArgs",
                extraBody = """
                    extensions.configure(site.addzero.ksp.route.gradle.RouteExtension::class.java) {
                        generatedPackage.set("demo.generated.route")
                        routeOwnerModule.set(layout.projectDirectory.dir("src/commonMain/kotlin").asFile.absolutePath)
                        moduleKey.set("feature-route")
                    }
                """.trimIndent(),
            ),
        )

        assertContains(
            output,
            "CONF[kspCommonMainMetadata]=site.addzero:route-processor",
            "CONF[commonMainImplementation]=site.addzero:route-core",
            "routeGenPkg=demo.generated.route",
            "routeModuleKey=feature-route",
            "TASK[compileKotlinJvm]=",
            "kspKotlinJvm",
        )
    }

    @Test
    fun `kmp options controller2api plugin serializes package and output dir`() {
        val output = runBuild(
            projectName = "controller2api-consumer",
            buildScript = kmpBuildScript(
                pluginClass = "site.addzero.ksp.controller2api.gradle.Controller2ApiGradlePlugin",
                serializedArgsKey = "site.addzero.kspconsumer.site.addzero.ksp.controller2api.serializedArgs",
                extraBody = """
                    extensions.configure(site.addzero.ksp.controller2api.gradle.Controller2ApiExtension::class.java) {
                        generatedPackage.set("demo.generated.api")
                    }
                """.trimIndent(),
            ),
        )

        assertContains(
            output,
            "CONF[kspCommonMainMetadata]=site.addzero:controller2api-processor",
            "apiClientPackageName=demo.generated.api",
            "apiClientOutputDir=",
        )
    }

    @Test
    fun `kmp options compose props plugin injects annotations companion`() {
        val output = runBuild(
            projectName = "compose-props-consumer",
            buildScript = kmpBuildScript(
                pluginClass = "site.addzero.ksp.composeprops.gradle.ComposePropsGradlePlugin",
                serializedArgsKey = "site.addzero.kspconsumer.site.addzero.ksp.compose-props.serializedArgs",
                extraBody = """
                    extensions.configure(site.addzero.ksp.composeprops.gradle.ComposePropsExtension::class.java) {
                        suffix.set("ViewState")
                    }
                """.trimIndent(),
            ),
        )

        assertContains(
            output,
            "CONF[kspCommonMainMetadata]=site.addzero:compose-props-processor",
            "CONF[commonMainImplementation]=site.addzero:compose-props-annotations",
            "COMPOSE_ATTRS_SUFFIX=ViewState",
        )
    }

    @Test
    fun `kmp options ioc plugin injects core companion`() {
        val output = runBuild(
            projectName = "ioc-consumer",
            buildScript = kmpBuildScript(
                pluginClass = "site.addzero.ksp.ioc.gradle.IocGradlePlugin",
                serializedArgsKey = "site.addzero.kspconsumer.site.addzero.ksp.ioc.serializedArgs",
                extraBody = """
                    extensions.configure(site.addzero.ksp.ioc.gradle.IocExtension::class.java) {
                        modulePackage.set("demo.generated.ioc")
                        app.set(true)
                    }
                """.trimIndent(),
            ),
        )

        assertContains(
            output,
            "CONF[kspCommonMainMetadata]=site.addzero:ioc-processor",
            "CONF[commonMainImplementation]=site.addzero:ioc-core",
            "ioc.role=app",
            "ioc.module=demo.generated.ioc",
        )
    }

    @Test
    fun `kmp umbrella plugin adds direct and spi subprocessors`() {
        val output = runBuild(
            projectName = "jimmer-external-consumer",
            buildScript = kmpBuildScript(
                pluginClass = "site.addzero.ksp.jimmerentityexternal.gradle.JimmerEntityExternalGradlePlugin",
                serializedArgsKey =
                    "site.addzero.kspconsumer.site.addzero.ksp.jimmer-entity-external.serializedArgs",
                extraBody = """
                    extensions.configure(
                        site.addzero.ksp.jimmerentityexternal.gradle.JimmerEntityExternalExtension::class.java
                    ) {
                        apiClientPackageName.set("demo.generated.api")
                        entity2Iso.packageName.set("demo.generated.isomorphic")
                        entity2Form.packageName.set("demo.generated.forms")
                        entity2Mcp.packageName.set("demo.generated.mcp")
                    }
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
            "entity2Form.enabled=true",
            "entity2Mcp.enabled=true",
            "isomorphicPkg=demo.generated.isomorphic",
            "formPackageName=demo.generated.forms",
            "mcpPackageName=demo.generated.mcp",
        )
    }

    @Test
    fun `kmp umbrella plugin serializes child processor enable flags`() {
        val output = runBuild(
            projectName = "jimmer-external-disabled-consumer",
            buildScript = kmpBuildScript(
                pluginClass = "site.addzero.ksp.jimmerentityexternal.gradle.JimmerEntityExternalGradlePlugin",
                serializedArgsKey =
                    "site.addzero.kspconsumer.site.addzero.ksp.jimmer-entity-external.serializedArgs",
                extraBody = """
                    extensions.configure(
                        site.addzero.ksp.jimmerentityexternal.gradle.JimmerEntityExternalExtension::class.java
                    ) {
                        entity2Iso.enabled.set(true)
                        entity2Iso.serializableEnabled.set(false)
                        entity2Form.enabled.set(false)
                        entity2Mcp.enabled.set(false)
                    }
                """.trimIndent(),
            ),
        )

        assertContains(
            output,
            "entity2Iso.enabled=true",
            "isomorphicSerializableEnabled=false",
            "entity2Form.enabled=false",
            "entity2Mcp.enabled=false",
        )
    }

    private fun jvmBuildScript(
        pluginClass: String,
        serializedArgsKey: String,
        extraBody: String,
    ): String {
        val kspConfiguration = "ksp".quoteForKotlin()
        val implementationConfiguration = "implementation".quoteForKotlin()
        val compileOnlyConfiguration = "compileOnly".quoteForKotlin()
        val quotedArgsKey = serializedArgsKey.quoteForKotlin()
        return """
            ${buildscriptBlock()}

            apply<org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper>()
            apply<$pluginClass>()

            $extraBody

            tasks.register("verifyPublishedKspPlugin") {
                doLast {
                    fun dumpConfiguration(name: String): String =
                        project.configurations.findByName(name)
                            ?.dependencies
                            ?.joinToString("|") { dependency ->
                                listOfNotNull(dependency.group, dependency.name).joinToString(":")
                            }
                            .orEmpty()
                    val kotlinExtension =
                        project.extensions.getByType(org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension::class.java)
                    val srcDirs = kotlinExtension.sourceSets.getByName("main").kotlin.srcDirs
                        .map { it.relativeTo(project.projectDir).path }
                        .sorted()
                        .joinToString("|")
                    val compileTask = project.tasks.getByName("compileKotlin")
                    val compileDeps = compileTask.taskDependencies.getDependencies(compileTask)
                        .map { it.name }
                        .sorted()
                        .joinToString("|")
                    println("CONF[ksp]=${'$'}{dumpConfiguration($kspConfiguration)}")
                    println("CONF[implementation]=${'$'}{dumpConfiguration($implementationConfiguration)}")
                    println("CONF[compileOnly]=${'$'}{dumpConfiguration($compileOnlyConfiguration)}")
                    println("SRC[jvm]=${'$'}srcDirs")
                    println("TASK[compileKotlin]=${'$'}compileDeps")
                    println("ARGS=${'$'}{project.extensions.extraProperties.get($quotedArgsKey)}")
                }
            }
        """.trimIndent()
    }

    private fun kmpBuildScript(
        pluginClass: String,
        serializedArgsKey: String,
        extraBody: String,
    ): String {
        val kspCommonMainMetadataConfiguration = "kspCommonMainMetadata".quoteForKotlin()
        val kspJvmConfiguration = "kspJvm".quoteForKotlin()
        val commonMainImplementationConfiguration = "commonMainImplementation".quoteForKotlin()
        val jvmMainImplementationConfiguration = "jvmMainImplementation".quoteForKotlin()
        val quotedArgsKey = serializedArgsKey.quoteForKotlin()
        return """
            ${buildscriptBlock()}

            apply<org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper>()
            extensions.configure(org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java) {
                jvm()
            }
            apply<$pluginClass>()

            $extraBody

            tasks.register("verifyPublishedKspPlugin") {
                doLast {
                    fun dumpConfiguration(name: String): String =
                        project.configurations.findByName(name)
                            ?.dependencies
                            ?.joinToString("|") { dependency ->
                                listOfNotNull(dependency.group, dependency.name).joinToString(":")
                            }
                            .orEmpty()
                    val kotlinExtension =
                        project.extensions.getByType(org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java)
                    val commonSrcDirs = kotlinExtension.sourceSets.getByName("commonMain").kotlin.srcDirs
                        .map { it.relativeTo(project.projectDir).path }
                        .sorted()
                        .joinToString("|")
                    val jvmSrcDirs = kotlinExtension.sourceSets.getByName("jvmMain").kotlin.srcDirs
                        .map { it.relativeTo(project.projectDir).path }
                        .sorted()
                        .joinToString("|")
                    val compileTask = project.tasks.getByName("compileKotlinJvm")
                    val compileDeps = compileTask.taskDependencies.getDependencies(compileTask)
                        .map { it.name }
                        .sorted()
                        .joinToString("|")
                    println("CONF[kspCommonMainMetadata]=${'$'}{dumpConfiguration($kspCommonMainMetadataConfiguration)}")
                    println("CONF[kspJvm]=${'$'}{dumpConfiguration($kspJvmConfiguration)}")
                    println("CONF[commonMainImplementation]=${'$'}{dumpConfiguration($commonMainImplementationConfiguration)}")
                    println("CONF[jvmMainImplementation]=${'$'}{dumpConfiguration($jvmMainImplementationConfiguration)}")
                    println("SRC[commonMain]=${'$'}commonSrcDirs")
                    println("SRC[jvmMain]=${'$'}jvmSrcDirs")
                    println("TASK[compileKotlinJvm]=${'$'}compileDeps")
                    println("ARGS=${'$'}{project.extensions.extraProperties.get($quotedArgsKey)}")
                }
            }
        """.trimIndent()
    }

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

package site.addzero.kcp.multireceiver.plugin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class MultireceiverCompilerIntegrationTest {

    @Test
    fun generates_extension_and_context_wrappers_without_jvm_signature_clashes() {
        val result = compile(
            mapOf(
                "site/addzero/example/TopLevelTargets.kt" to topLevelTargetsSource(),
            ),
        )

        assertEquals(0, result.exitCode, result.output)

        val targetsKt = result.loadClass("site.addzero.example.TopLevelTargetsKt")
        assertEquals("<ok>", targetsKt.getMethod("invokeTopLevelWrap").invoke(null))
        assertEquals("svc:scope:7", targetsKt.getMethod("invokeTopLevelRender").invoke(null))
        assertEquals("eng[ok]", targetsKt.getMethod("invokeMemberWrap").invoke(null))
        assertEquals("eng:svc:9", targetsKt.getMethod("invokeMemberRender").invoke(null))

        val topLevelMethods = targetsKt.declaredMethods.map { method -> method.name }.toSet()
        assertTrue("wrap" in topLevelMethods, topLevelMethods.toString())

        val generatedTopLevelClass = result.loadClass("site.addzero.example.__GENERATED__CALLABLES__Kt")
        val generatedTopLevelMethods = generatedTopLevelClass.declaredMethods.map { method -> method.name }.toSet()
        assertTrue(
            generatedTopLevelMethods.any { methodName -> methodName.startsWith("wrapByAddzeroExtension") },
            generatedTopLevelMethods.toString(),
        )
        assertTrue(
            generatedTopLevelMethods.any { methodName -> methodName.startsWith("renderByAddzeroContext") },
            generatedTopLevelMethods.toString(),
        )

        val engineClass = result.loadClass("site.addzero.example.Engine")
        val engineMethods = engineClass.declaredMethods.map { method -> method.name }.toSet()
        assertTrue(
            engineMethods.any { methodName -> methodName.startsWith("decorateByAddzeroExtension") },
            engineMethods.toString(),
        )
        assertTrue(
            engineMethods.any { methodName -> methodName.startsWith("computeByAddzeroContext") },
            engineMethods.toString(),
        )
    }

    @Test
    fun rejects_invalid_receiver_parameter_shapes() {
        val result = compile(
            mapOf(
                "site/addzero/example/BrokenTargets.kt" to """
                    package site.addzero.example

                    import site.addzero.kcp.annotations.GenerateExtension
                    import site.addzero.kcp.annotations.Receiver

                    @GenerateExtension
                    fun broken(@Receiver value: String = "oops", other: Int): String = "${'$'}value:${'$'}other"
                """.trimIndent(),
            ),
        )

        assertTrue(result.exitCode != 0, result.output)
        assertTrue(
            result.output.contains("context parameters cannot declare default values"),
            result.output,
        )
    }

    private fun compile(sources: Map<String, String>): CompilationResult {
        val workingDir = Files.createTempDirectory("multireceiver-it")
        val sourceDir = workingDir.resolve("src").createDirectories()
        val classesDir = workingDir.resolve("classes").createDirectories()

        sources.forEach { (relativePath, content) ->
            val file = sourceDir.resolve(relativePath)
            file.parent.createDirectories()
            file.writeText(content)
        }

        val pluginJar = System.getProperty("multireceiver.pluginJar")
            ?: error("Missing multireceiver.pluginJar system property")
        val classpath = System.getProperty("java.class.path")
        val command = mutableListOf(
            javaExecutable(),
            "-cp",
            classpath,
            "org.jetbrains.kotlin.cli.jvm.K2JVMCompiler",
            "-Xjvm-default=all",
            "-Xcontext-parameters",
            "-language-version",
            "2.2",
            "-d",
            classesDir.toString(),
            "-classpath",
            classpath,
            "-Xplugin=$pluginJar",
        )
        command += sources.keys
            .sorted()
            .map { relativePath -> sourceDir.resolve(relativePath).toString() }

        val process = ProcessBuilder(command)
            .directory(workingDir.toFile())
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        return CompilationResult(
            exitCode = exitCode,
            output = output,
            classesDir = classesDir,
            runtimeClasspath = classpath.split(File.pathSeparator).filter(String::isNotBlank),
        )
    }

    private fun javaExecutable(): String {
        val javaHome = System.getProperty("java.home")
        return Paths.get(javaHome, "bin", "java").toString()
    }

    private fun topLevelTargetsSource(): String {
        return """
            package site.addzero.example

            import site.addzero.kcp.annotations.GenerateExtension
            import site.addzero.kcp.annotations.Receiver

            data class Service(val prefix: String)
            data class Scope(val label: String)

            @GenerateExtension
            fun wrap(value: String): String = "<${'$'}value>"

            @GenerateExtension
            fun render(@Receiver service: Service, @Receiver scope: Scope, value: Int): String =
                "${'$'}{service.prefix}:${'$'}{scope.label}:${'$'}value"

            class Engine(private val prefix: String) {
                @GenerateExtension
                fun decorate(value: String): String = "${'$'}prefix[${'$'}value]"

                @GenerateExtension
                fun compute(@Receiver service: Service, value: Int): String =
                    "${'$'}prefix:${'$'}{service.prefix}:${'$'}value"
            }

            fun invokeTopLevelWrap(): String = "ok".wrap()

            fun invokeTopLevelRender(): String = context(Service("svc"), Scope("scope")) {
                render(7)
            }

            fun invokeMemberWrap(): String = with(Engine("eng")) {
                "ok".decorate()
            }

            fun invokeMemberRender(): String = with(Engine("eng")) {
                context(Service("svc")) {
                    compute(9)
                }
            }
        """.trimIndent()
    }

    private data class CompilationResult(
        val exitCode: Int,
        val output: String,
        val classesDir: Path,
        val runtimeClasspath: List<String>,
    ) {
        fun loadClass(name: String): Class<*> {
            val urls = buildList {
                add(classesDir.toUri().toURL())
                runtimeClasspath
                    .map(::File)
                    .filter(File::exists)
                    .forEach { file -> add(file.toURI().toURL()) }
            }.toTypedArray()
            val classLoader = URLClassLoader(urls, javaClass.classLoader)
            return classLoader.loadClass(name)
        }
    }
}

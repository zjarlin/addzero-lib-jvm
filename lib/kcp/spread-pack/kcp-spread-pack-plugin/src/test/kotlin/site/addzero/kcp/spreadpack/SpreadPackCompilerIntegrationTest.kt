package site.addzero.kcp.spreadpack

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

class SpreadPackCompilerIntegrationTest {

    @Test
    fun generates_member_top_level_and_renamed_overloads() {
        val result = compile(
            mapOf(
                "site/addzero/example/SpreadPackTargets.kt" to spreadPackTargetsSource(),
            ),
        )

        assertEquals(0, result.exitCode, result.output)

        val targetsKt = result.loadClass("site.addzero.example.SpreadPackTargetsKt")
        assertEquals("alpha:2:done:true", targetsKt.getMethod("invokeMemberProps").invoke(null))
        assertEquals("attrs:7:false:-", targetsKt.getMethod("invokeTopLevelAttrs").invoke(null))
        assertEquals("callback", targetsKt.getMethod("invokeTopLevelCallbacks").invoke(null))

        val rendererClass = result.loadClass("site.addzero.example.Renderer")
        val methods = rendererClass.declaredMethods.toList()
        assertTrue(
            methods.any { method ->
                method.name == "render" &&
                    method.parameterTypes.map(Class<*>::getName) == listOf(
                        "java.lang.String",
                        "int",
                        "kotlin.jvm.functions.Function0",
                        "boolean",
                    )
            },
            methods.joinToString("\n"),
        )
    }

    @Test
    fun rejects_excluding_required_field_without_default() {
        val result = compile(
            mapOf(
                "site/addzero/example/BrokenTargets.kt" to """
                    package site.addzero.example

                    import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
                    import site.addzero.kcp.spreadpack.SpreadPack

                    data class BrokenOptions(
                        val required: String,
                        val optional: Int = 0,
                    )

                    @GenerateSpreadPackOverloads
                    fun broken(
                        @SpreadPack(exclude = ["required"])
                        options: BrokenOptions,
                    ): String = options.required
                """.trimIndent(),
            ),
        )

        assertTrue(result.exitCode != 0, result.output)
        assertTrue(
            result.output.contains("cannot omit required field required"),
            result.output,
        )
    }

    private fun compile(sources: Map<String, String>): CompilationResult {
        val workingDir = Files.createTempDirectory("spread-pack-it")
        val sourceDir = workingDir.resolve("src").createDirectories()
        val classesDir = workingDir.resolve("classes").createDirectories()

        sources.forEach { (relativePath, content) ->
            val file = sourceDir.resolve(relativePath)
            file.parent.createDirectories()
            file.writeText(content)
        }

        val pluginJar = System.getProperty("spreadPack.pluginJar")
            ?: error("Missing spreadPack.pluginJar system property")
        val classpath = System.getProperty("java.class.path")
        val command = mutableListOf(
            javaExecutable(),
            "-cp",
            classpath,
            "org.jetbrains.kotlin.cli.jvm.K2JVMCompiler",
            "-no-stdlib",
            "-no-reflect",
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

    private fun spreadPackTargetsSource(): String {
        return """
            package site.addzero.example

            import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
            import site.addzero.kcp.spreadpack.SpreadPack
            import site.addzero.kcp.spreadpack.SpreadPackSelector

            data class RenderOptions(
                val title: String = "untitled",
                val count: Int = 0,
                val onDone: (() -> String)? = null,
                val debug: Boolean = false,
            )

            @GenerateSpreadPackOverloads
            class Renderer {
                fun render(@SpreadPack options: RenderOptions): String {
                    val done = options.onDone?.invoke() ?: "-"
                    return "${'$'}{options.title}:${'$'}{options.count}:${'$'}done:${'$'}{options.debug}"
                }
            }

            @GenerateSpreadPackOverloads
            fun renderAttrs(
                @SpreadPack(
                    selector = SpreadPackSelector.ATTRS,
                    exclude = ["debug"],
                )
                options: RenderOptions,
            ): String {
                val done = options.onDone?.invoke() ?: "-"
                return "${'$'}{options.title}:${'$'}{options.count}:${'$'}{options.debug}:${'$'}done"
            }

            @GenerateSpreadPackOverloads
            fun renderCallbacks(
                @SpreadPack(selector = SpreadPackSelector.CALLBACKS)
                options: RenderOptions,
            ): String = options.onDone?.invoke() ?: "none"

            fun invokeMemberProps(): String =
                Renderer().render(
                    title = "alpha",
                    count = 2,
                    onDone = { "done" },
                    debug = true,
                )

            fun invokeTopLevelAttrs(): String =
                renderAttrs(
                    title = "attrs",
                    count = 7,
                )

            fun invokeTopLevelCallbacks(): String =
                renderCallbacks(
                    onDone = { "callback" },
                )
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

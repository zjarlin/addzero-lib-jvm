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

    @Test
    fun generates_argsof_overload_from_selected_referenced_overload() {
        val result = compile(
            mapOf(
                "site/addzero/example/ArgsofTargets.kt" to argsofTargetsSource(),
            ),
        )

        assertEquals(0, result.exitCode, result.output)

        val targetsKt = result.loadClass("site.addzero.example.ArgsofTargetsKt")
        assertEquals("wrapped:3:done", targetsKt.getMethod("invokeArgsof").invoke(null))

        val methods = targetsKt.declaredMethods.toList()
        assertTrue(
            methods.any { method ->
                method.name == "renderWrapper" &&
                    method.parameterTypes.map(Class<*>::getName) == listOf(
                        "java.lang.String",
                        "int",
                        "kotlin.jvm.functions.Function0",
                    )
            },
            methods.joinToString("\n"),
        )
    }

    @Test
    fun flattens_nested_argsof_references() {
        val result = compile(
            mapOf(
                "site/addzero/example/NestedArgsofTargets.kt" to nestedArgsofTargetsSource(),
            ),
        )

        assertEquals(0, result.exitCode, result.output)

        val targetsKt = result.loadClass("site.addzero.example.NestedArgsofTargetsKt")
        assertEquals("nested:chain", targetsKt.getMethod("invokeNestedArgsof").invoke(null))
    }

    @Test
    fun rejects_ambiguous_argsof_overload_set_without_parameter_types() {
        val result = compile(
            mapOf(
                "site/addzero/example/AmbiguousArgsofTargets.kt" to ambiguousArgsofTargetsSource(),
            ),
        )

        assertTrue(result.exitCode != 0, result.output)
        assertTrue(
            result.output.contains("is ambiguous; specify SpreadOverload.parameterTypes"),
            result.output,
        )
    }

    @Test
    fun rejects_argsof_cycles() {
        val result = compile(
            mapOf(
                "site/addzero/example/CyclicArgsofTargets.kt" to cyclicArgsofTargetsSource(),
            ),
        )

        assertTrue(result.exitCode != 0, result.output)
        assertTrue(
            result.output.contains("detected argsof overload cycle"),
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

    private fun argsofTargetsSource(): String {
        return """
            package site.addzero.example

            import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
            import site.addzero.kcp.spreadpack.SpreadArgsOf
            import site.addzero.kcp.spreadpack.SpreadOverload
            import site.addzero.kcp.spreadpack.SpreadOverloadsOf
            import site.addzero.kcp.spreadpack.SpreadPack

            data class BaseOptions(
                val title: String = "",
                val count: Int = 0,
                val debug: Boolean = false,
                val onDone: (() -> String)? = null,
            )

            @GenerateSpreadPackOverloads
            fun renderBase(@SpreadPack options: BaseOptions): String {
                val done = options.onDone?.invoke() ?: "-"
                return "${'$'}{options.title}:${'$'}{options.count}:${'$'}{options.debug}:${'$'}done"
            }

            fun renderBase(title: String): String = title

            data class WrapperArgs(
                val title: String = "",
                val count: Int = 0,
                val onDone: (() -> String)? = null,
            )

            @GenerateSpreadPackOverloads
            fun renderWrapper(
                @SpreadPack
                @SpreadArgsOf(
                    overload = SpreadOverload(
                        of = SpreadOverloadsOf("site.addzero.example.renderBase"),
                        parameterTypes = [BaseOptions::class],
                    ),
                    exclude = ["debug"],
                )
                args: WrapperArgs,
            ): String {
                val done = args.onDone?.invoke() ?: "-"
                return "${'$'}{args.title}:${'$'}{args.count}:${'$'}done"
            }

            fun invokeArgsof(): String =
                renderWrapper(
                    title = "wrapped",
                    count = 3,
                    onDone = { "done" },
                )
        """.trimIndent()
    }

    private fun nestedArgsofTargetsSource(): String {
        return """
            package site.addzero.example

            import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
            import site.addzero.kcp.spreadpack.SpreadArgsOf
            import site.addzero.kcp.spreadpack.SpreadOverload
            import site.addzero.kcp.spreadpack.SpreadOverloadsOf
            import site.addzero.kcp.spreadpack.SpreadPack

            data class LeafArgs(
                val title: String = "",
                val onDone: (() -> String)? = null,
            )

            @GenerateSpreadPackOverloads
            fun leaf(@SpreadPack args: LeafArgs): String =
                args.onDone?.invoke() ?: args.title

            data class MiddleArgs(
                val title: String = "",
                val onDone: (() -> String)? = null,
            )

            @GenerateSpreadPackOverloads
            fun middle(
                @SpreadPack
                @SpreadArgsOf(
                    overload = SpreadOverload(
                        of = SpreadOverloadsOf("site.addzero.example.leaf"),
                        parameterTypes = [LeafArgs::class],
                    ),
                )
                args: MiddleArgs,
            ): String = args.onDone?.invoke() ?: args.title

            data class OuterArgs(
                val title: String = "",
                val onDone: (() -> String)? = null,
            )

            @GenerateSpreadPackOverloads
            fun outer(
                @SpreadPack
                @SpreadArgsOf(
                    overload = SpreadOverload(
                        of = SpreadOverloadsOf("site.addzero.example.middle"),
                        parameterTypes = [MiddleArgs::class],
                    ),
                )
                args: OuterArgs,
            ): String {
                val done = args.onDone?.invoke() ?: "-"
                return "${'$'}{args.title}:${'$'}done"
            }

            fun invokeNestedArgsof(): String =
                outer(
                    title = "nested",
                    onDone = { "chain" },
                )
        """.trimIndent()
    }

    private fun ambiguousArgsofTargetsSource(): String {
        return """
            package site.addzero.example

            import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
            import site.addzero.kcp.spreadpack.SpreadArgsOf
            import site.addzero.kcp.spreadpack.SpreadOverload
            import site.addzero.kcp.spreadpack.SpreadOverloadsOf
            import site.addzero.kcp.spreadpack.SpreadPack

            data class ChoiceArgs(
                val title: String = "",
            )

            @GenerateSpreadPackOverloads
            fun renderChoice(@SpreadPack args: ChoiceArgs): String = args.title

            fun renderChoice(title: String): String = title

            data class WrapperArgs(
                val title: String = "",
            )

            @GenerateSpreadPackOverloads
            fun renderWrapper(
                @SpreadPack
                @SpreadArgsOf(
                    overload = SpreadOverload(
                        of = SpreadOverloadsOf("site.addzero.example.renderChoice"),
                    ),
                )
                args: WrapperArgs,
            ): String = args.title
        """.trimIndent()
    }

    private fun cyclicArgsofTargetsSource(): String {
        return """
            package site.addzero.example

            import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
            import site.addzero.kcp.spreadpack.SpreadArgsOf
            import site.addzero.kcp.spreadpack.SpreadOverload
            import site.addzero.kcp.spreadpack.SpreadOverloadsOf
            import site.addzero.kcp.spreadpack.SpreadPack

            data class LoopArgs(
                val title: String = "",
            )

            @GenerateSpreadPackOverloads
            fun renderA(
                @SpreadPack
                @SpreadArgsOf(
                    overload = SpreadOverload(
                        of = SpreadOverloadsOf("site.addzero.example.renderB"),
                        parameterTypes = [LoopArgs::class],
                    ),
                )
                args: LoopArgs,
            ): String = args.title

            @GenerateSpreadPackOverloads
            fun renderB(
                @SpreadPack
                @SpreadArgsOf(
                    overload = SpreadOverload(
                        of = SpreadOverloadsOf("site.addzero.example.renderA"),
                        parameterTypes = [LoopArgs::class],
                    ),
                )
                args: LoopArgs,
            ): String = args.title
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

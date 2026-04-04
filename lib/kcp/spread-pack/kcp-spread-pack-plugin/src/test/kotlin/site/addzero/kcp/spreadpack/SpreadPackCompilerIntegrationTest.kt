package site.addzero.kcp.spreadpack

import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
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

        val generatedCallablesKt = result.loadClass("site.addzero.example.__GENERATED__CALLABLES__Kt")
        val methods = generatedCallablesKt.declaredMethods.toList()
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

    @Test
    fun applies_argsof_selector_and_exclude_to_flattened_fields() {
        val result = compile(
            mapOf(
                "site/addzero/example/SelectorArgsofTargets.kt" to selectorArgsofTargetsSource(),
            ),
        )

        assertEquals(0, result.exitCode, result.output)

        val targetsKt = result.loadClass("site.addzero.example.SelectorArgsofTargetsKt")
        assertEquals("attrs:4", targetsKt.getMethod("invokeAttrArgsof").invoke(null))
        assertEquals("done", targetsKt.getMethod("invokeCallbackArgsof").invoke(null))

        val generatedCallablesKt = result.loadClass("site.addzero.example.__GENERATED__CALLABLES__Kt")
        val methods = generatedCallablesKt.declaredMethods.toList()
        assertTrue(
            methods.any { method ->
                method.name == "renderAttrWrapper" &&
                    method.parameterTypes.map(Class<*>::getName) == listOf(
                        "java.lang.String",
                        "int",
                    )
            },
            methods.joinToString("\n"),
        )
        assertTrue(
            methods.any { method ->
                method.name == "renderCallbackWrapper" &&
                    method.parameterTypes.map(Class<*>::getName) == listOf(
                        "kotlin.jvm.functions.Function0",
                    )
            },
            methods.joinToString("\n"),
        )
    }

    @Test
    fun resolves_argsof_member_function_overload_sets() {
        val result = compile(
            mapOf(
                "site/addzero/example/MemberArgsofTargets.kt" to memberArgsofTargetsSource(),
            ),
        )

        assertEquals(0, result.exitCode, result.output)

        val targetsKt = result.loadClass("site.addzero.example.MemberArgsofTargetsKt")
        assertEquals("member:5:done", targetsKt.getMethod("invokeMemberArgsof").invoke(null))
    }

    @Test
    fun generates_annotated_empty_carrier_from_referenced_function() {
        val result = compile(
            mapOf(
                "site/addzero/example/GeneratedCarrierTargets.kt" to generatedCarrierTargetsSource(),
            ),
        )

        assertEquals(0, result.exitCode, result.output)

        val targetsKt = result.loadClass("site.addzero.example.GeneratedCarrierTargetsKt")
        assertEquals("untitled:3:true", targetsKt.getMethod("invokeGeneratedCarrier").invoke(null))

        val generatedCarrierClass = result.loadClass("site.addzero.example.RenderAliasArgs")
        assertTrue(
            generatedCarrierClass.declaredMethods.any { method -> method.name == "getTitle" },
            generatedCarrierClass.declaredMethods.joinToString("\n"),
        )

        val generatedCallablesKt = result.loadClass("site.addzero.example.__GENERATED__CALLABLES__Kt")
        val methods = generatedCallablesKt.declaredMethods.toList()
        assertTrue(
            methods.any { method ->
                method.name == "renderAlias" &&
                    method.parameterTypes.map(Class<*>::getName) == listOf(
                        "java.lang.String",
                        "int",
                    )
            },
            methods.joinToString("\n"),
        )
    }

    @Test
    fun marks_generated_overloads_with_source_metadata() {
        val memberResult = compile(
            mapOf(
                "site/addzero/example/SpreadPackTargets.kt" to spreadPackTargetsSource(),
            ),
        )
        assertEquals(0, memberResult.exitCode, memberResult.output)

        val rendererClass = memberResult.loadClass("site.addzero.example.Renderer")
        val memberGeneratedMethod = rendererClass.declaredMethods.single { method ->
            method.name == "render" &&
                method.parameterTypes.map(Class<*>::getName) == listOf(
                    "java.lang.String",
                    "int",
                    "kotlin.jvm.functions.Function0",
                    "boolean",
                )
        }
        assertEquals(
            "site.addzero.example.Renderer.render",
            memberResult.generatedOverloadSourceFunctionFqName(
                className = "site.addzero.example.Renderer",
                methodName = memberGeneratedMethod.name,
                methodDescriptor = Type.getMethodDescriptor(memberGeneratedMethod),
            ),
        )

        val topLevelResult = compile(
            mapOf(
                "site/addzero/example/ArgsofTargets.kt" to argsofTargetsSource(),
            ),
        )
        assertEquals(0, topLevelResult.exitCode, topLevelResult.output)

        val generatedCallablesKt = topLevelResult.loadClass("site.addzero.example.__GENERATED__CALLABLES__Kt")
        val topLevelGeneratedMethod = generatedCallablesKt.declaredMethods.single { method ->
            method.name == "renderWrapper" &&
                method.parameterTypes.map(Class<*>::getName) == listOf(
                    "java.lang.String",
                    "int",
                    "kotlin.jvm.functions.Function0",
                )
        }
        assertEquals(
            "site.addzero.example.renderWrapper",
            topLevelResult.generatedOverloadSourceFunctionFqName(
                className = "site.addzero.example.__GENERATED__CALLABLES__Kt",
                methodName = topLevelGeneratedMethod.name,
                methodDescriptor = Type.getMethodDescriptor(topLevelGeneratedMethod),
            ),
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
                    "site.addzero.example.renderBase",
                    parameterTypes = [BaseOptions::class],
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

    private fun selectorArgsofTargetsSource(): String {
        return """
            package site.addzero.example

            import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
            import site.addzero.kcp.spreadpack.SpreadArgsOf
            import site.addzero.kcp.spreadpack.SpreadOverload
            import site.addzero.kcp.spreadpack.SpreadOverloadsOf
            import site.addzero.kcp.spreadpack.SpreadPack
            import site.addzero.kcp.spreadpack.SpreadPackSelector

            data class SourceArgs(
                val title: String = "",
                val count: Int = 0,
                val debug: Boolean = false,
                val onDone: (() -> String)? = null,
                val onCancel: (() -> String)? = null,
            )

            @GenerateSpreadPackOverloads
            fun renderSource(@SpreadPack args: SourceArgs): String {
                val done = args.onDone?.invoke() ?: "-"
                val cancel = args.onCancel?.invoke() ?: "-"
                return "${'$'}{args.title}:${'$'}{args.count}:${'$'}{args.debug}:${'$'}done:${'$'}cancel"
            }

            data class AttrWrapperArgs(
                val title: String = "",
                val count: Int = 0,
            )

            @GenerateSpreadPackOverloads
            fun renderAttrWrapper(
                @SpreadPack
                @SpreadArgsOf(
                    overload = SpreadOverload(
                        of = SpreadOverloadsOf("site.addzero.example.renderSource"),
                        parameterTypes = [SourceArgs::class],
                    ),
                    selector = SpreadPackSelector.ATTRS,
                    exclude = ["debug"],
                )
                args: AttrWrapperArgs,
            ): String = "${'$'}{args.title}:${'$'}{args.count}"

            data class CallbackWrapperArgs(
                val onDone: (() -> String)? = null,
            )

            @GenerateSpreadPackOverloads
            fun renderCallbackWrapper(
                @SpreadPack
                @SpreadArgsOf(
                    overload = SpreadOverload(
                        of = SpreadOverloadsOf("site.addzero.example.renderSource"),
                        parameterTypes = [SourceArgs::class],
                    ),
                    selector = SpreadPackSelector.CALLBACKS,
                    exclude = ["onCancel"],
                )
                args: CallbackWrapperArgs,
            ): String = args.onDone?.invoke() ?: "-"

            fun invokeAttrArgsof(): String =
                renderAttrWrapper(
                    title = "attrs",
                    count = 4,
                )

            fun invokeCallbackArgsof(): String =
                renderCallbackWrapper(
                    onDone = { "done" },
                )
        """.trimIndent()
    }

    private fun memberArgsofTargetsSource(): String {
        return """
            package site.addzero.example

            import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
            import site.addzero.kcp.spreadpack.SpreadArgsOf
            import site.addzero.kcp.spreadpack.SpreadOverload
            import site.addzero.kcp.spreadpack.SpreadOverloadsOf
            import site.addzero.kcp.spreadpack.SpreadPack

            data class MemberBaseArgs(
                val title: String = "",
                val count: Int = 0,
                val debug: Boolean = false,
                val onDone: (() -> String)? = null,
            )

            data class MemberWrapperArgs(
                val title: String = "",
                val count: Int = 0,
                val onDone: (() -> String)? = null,
            )

            class MemberRenderer {
                @GenerateSpreadPackOverloads
                fun renderBase(@SpreadPack args: MemberBaseArgs): String {
                    val done = args.onDone?.invoke() ?: "-"
                    return "${'$'}{args.title}:${'$'}{args.count}:${'$'}{args.debug}:${'$'}done"
                }

                fun renderBase(title: String): String = title
            }

            @GenerateSpreadPackOverloads
            fun renderWrapper(
                @SpreadPack
                @SpreadArgsOf(
                    overload = SpreadOverload(
                        of = SpreadOverloadsOf("site.addzero.example.MemberRenderer.renderBase"),
                        parameterTypes = [MemberBaseArgs::class],
                    ),
                    exclude = ["debug"],
                )
                args: MemberWrapperArgs,
            ): String {
                val done = args.onDone?.invoke() ?: "-"
                return "${'$'}{args.title}:${'$'}{args.count}:${'$'}done"
            }

            fun invokeMemberArgsof(): String =
                renderWrapper(
                    title = "member",
                    count = 5,
                    onDone = { "done" },
                )
        """.trimIndent()
    }

    private fun generatedCarrierTargetsSource(): String {
        return """
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

            fun invokeGeneratedCarrier(): String = renderAlias(count = 3)
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

        fun generatedOverloadSourceFunctionFqName(
            className: String,
            methodName: String,
            methodDescriptor: String,
        ): String? {
            val classBytes = Files.readAllBytes(classesDir.resolve(className.replace('.', '/') + ".class"))
            var sourceFunctionFqName: String? = null
            ClassReader(classBytes).accept(
                object : ClassVisitor(Opcodes.ASM9) {
                    override fun visitMethod(
                        access: Int,
                        name: String,
                        descriptor: String,
                        signature: String?,
                        exceptions: Array<out String>?,
                    ): MethodVisitor? {
                        if (name != methodName || descriptor != methodDescriptor) {
                            return null
                        }
                        return object : MethodVisitor(Opcodes.ASM9) {
                            override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
                                if (descriptor != GENERATED_SPREAD_PACK_OVERLOAD_DESCRIPTOR) {
                                    return null
                                }
                                return object : AnnotationVisitor(Opcodes.ASM9) {
                                    override fun visit(name: String, value: Any) {
                                        if (name == "sourceFunctionFqName") {
                                            sourceFunctionFqName = value as String
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES,
            )
            return sourceFunctionFqName
        }

        private companion object {
            const val GENERATED_SPREAD_PACK_OVERLOAD_DESCRIPTOR =
                "Lsite/addzero/kcp/spreadpack/GeneratedSpreadPackOverload;"
        }
    }
}

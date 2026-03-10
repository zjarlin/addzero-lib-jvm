package site.addzero.kcp.transformoverload.plugin

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

class TransformOverloadCompilerIntegrationTest {

    @Test
    fun generates_overloads_for_direct_lifting_cartesian_and_top_level_targets() {
        val result = compile(
            mapOf(
                "org/babyfish/jimmer/Stubs.kt" to jimmerStubsSource(),
                "org/babyfish/jimmer/spring/repo/RepositoryTargets.kt" to repositoryTargetsSource(),
                "org/babyfish/jimmer/spring/repo/RepositoryUsage.kt" to repositoryUsageSource(),
            ),
        )

        assertEquals(0, result.exitCode, result.output)

        result.loadClass("org.babyfish.jimmer.spring.repo.KotlinRepository").let { repositoryClass ->
            val methods = repositoryClass.declaredMethods.toList()
            val methodNames = methods.map { method -> method.name }.toSet()
            assertTrue("saveAllViaToEntityInput" in methodNames, methodNames.toString())
            assertTrue("saveAllViaFromDraft" in methodNames, methodNames.toString())
            assertTrue("saveListViaToEntityInput" in methodNames, methodNames.toString())
            assertTrue("saveSetViaToEntityInput" in methodNames, methodNames.toString())
            assertTrue("saveSeqViaToEntityInput" in methodNames, methodNames.toString())

            assertTrue(
                methods.any { method ->
                    method.name == "save" &&
                        method.parameterTypes.singleOrNull()?.name == "org.babyfish.jimmer.Input"
                },
                methods.joinToString("\n"),
            )
            assertTrue(
                methods.any { method ->
                    method.name == "save" &&
                        method.parameterTypes.singleOrNull()?.name == "org.babyfish.jimmer.Draft"
                },
                methods.joinToString("\n"),
            )
            assertTrue(
                methods.any { method ->
                    method.name == "save" &&
                        method.parameterTypes.singleOrNull()?.name == "org.babyfish.jimmer.AltInput"
                },
                methods.joinToString("\n"),
            )

            assertTrue(
                methods.any { method ->
                    method.name == "pair" &&
                        method.parameterTypes.map(Class<*>::getName) == listOf(
                            "org.babyfish.jimmer.Input",
                            "java.lang.Object",
                        )
                },
                methods.joinToString("\n"),
            )
            assertTrue(
                methods.any { method ->
                    method.name == "pair" &&
                        method.parameterTypes.map(Class<*>::getName) == listOf(
                            "java.lang.Object",
                            "org.babyfish.jimmer.Input",
                        )
                },
                methods.joinToString("\n"),
            )
            assertTrue(
                methods.any { method ->
                    method.name == "pair" &&
                        method.parameterTypes.map(Class<*>::getName) == listOf(
                            "org.babyfish.jimmer.Input",
                            "org.babyfish.jimmer.Input",
                        )
                },
                methods.joinToString("\n"),
            )

            assertTrue(
                methods.any { method ->
                    method.name == "pick" &&
                        method.parameterTypes.firstOrNull()?.name == "org.babyfish.jimmer.Input"
                },
                methods.joinToString("\n"),
            )
        }

        result.loadClass("org.babyfish.jimmer.spring.repo.MethodLevelRepository").let { methodRepoClass ->
            val methods = methodRepoClass.declaredMethods.toList()
            assertTrue(
                methods.any { method ->
                    method.name == "only" &&
                        method.parameterTypes.singleOrNull()?.name == "org.babyfish.jimmer.Input"
                },
                methods.joinToString("\n"),
            )
            assertTrue(
                methods.none { method ->
                    method.name == "untouched" &&
                        method.parameterTypes.singleOrNull()?.name == "org.babyfish.jimmer.Input"
                },
                methods.joinToString("\n"),
            )
        }

        assertTrue(
            result.hasDeclaredMethodInPackage("org.babyfish.jimmer.spring.repo") { method ->
                method.name == "topSaveStringViaToEntityInput" &&
                    method.parameterTypes.singleOrNull()?.name == "org.babyfish.jimmer.Input"
            },
            "Expected generated top-level overload in compiled package classes",
        )
    }

    @Test
    fun rejects_invalid_converter_shape() {
        val result = compile(
            mapOf(
                "org/babyfish/jimmer/Stubs.kt" to jimmerStubsSource(),
                "org/babyfish/jimmer/spring/repo/BrokenTargets.kt" to """
                    package org.babyfish.jimmer.spring.repo

                    import site.addzero.kcp.transformoverload.annotations.GenerateTransformOverloads
                    import site.addzero.kcp.transformoverload.annotations.OverloadTransform

                    @OverloadTransform
                    fun broken(source: String, other: String): Int = source.length + other.length

                    @GenerateTransformOverloads
                    interface BrokenRepository {
                        fun save(value: Int): String
                    }
                """.trimIndent(),
            ),
        )

        assertTrue(result.exitCode != 0, result.output)
        assertTrue(
            result.output.contains("Invalid @OverloadTransform converter"),
            result.output,
        )
    }

    @Test
    fun fails_when_renamed_signature_still_conflicts() {
        val result = compile(
            mapOf(
                "org/babyfish/jimmer/Stubs.kt" to jimmerStubsSource(),
                "org/babyfish/jimmer/spring/repo/ConflictTargets.kt" to """
                    package org.babyfish.jimmer.spring.repo

                    import org.babyfish.jimmer.Draft
                    import org.babyfish.jimmer.Input
                    import site.addzero.kcp.transformoverload.annotations.GenerateTransformOverloads
                    import site.addzero.kcp.transformoverload.annotations.OverloadTransform

                    @OverloadTransform
                    fun <E : Any> Input<E>.toEntity(): E = toEntity()

                    @OverloadTransform
                    fun <E : Any> Draft<E>.toEntity(): E = toEntity()

                    @GenerateTransformOverloads
                    interface ConflictRepository<E : Any> {
                        fun saveAll(values: Iterable<E>): String
                    }
                """.trimIndent(),
            ),
        )

        assertTrue(result.exitCode != 0, result.output)
        assertTrue(
            result.output.contains("Transform overload rename conflict"),
            result.output,
        )
    }

    private fun compile(sources: Map<String, String>): CompilationResult {
        val workingDir = Files.createTempDirectory("transform-overload-it")
        val sourceDir = workingDir.resolve("src").createDirectories()
        val classesDir = workingDir.resolve("classes").createDirectories()

        sources.forEach { (relativePath, content) ->
            val file = sourceDir.resolve(relativePath)
            file.parent.createDirectories()
            file.writeText(content)
        }

        val pluginJar = System.getProperty("transformOverload.pluginJar")
            ?: error("Missing transformOverload.pluginJar system property")
        val classpath = System.getProperty("java.class.path")
        val command = mutableListOf(
            javaExecutable(),
            "-cp",
            classpath,
            "org.jetbrains.kotlin.cli.jvm.K2JVMCompiler",
            "-Xjvm-default=all",
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

    private fun jimmerStubsSource(): String {
        return """
            package org.babyfish.jimmer

            interface Input<E> {
                fun toEntity(): E
            }

            interface Draft<E> {
                fun toEntity(): E
            }

            interface AltInput<E> {
                fun unwrap(): E
            }
        """.trimIndent()
    }

    private fun repositoryTargetsSource(): String {
        return """
            package org.babyfish.jimmer.spring.repo

            import org.babyfish.jimmer.AltInput
            import org.babyfish.jimmer.Draft
            import org.babyfish.jimmer.Input
            import site.addzero.kcp.transformoverload.annotations.GenerateTransformOverloads
            import site.addzero.kcp.transformoverload.annotations.OverloadTransform
            import site.addzero.kcp.transformoverload.annotations.TransformProvider

            @OverloadTransform
            fun <E : Any> Input<E>.toEntityInput(): E = toEntity()

            @OverloadTransform
            fun <E : Any> AltInput<E>.unwrapAlt(): E = unwrap()

            object DraftProvider : TransformProvider {
                @OverloadTransform
                fun <E : Any> Draft<E>.fromDraft(): E = toEntity()
            }

            @GenerateTransformOverloads
            interface KotlinRepository<E : Any> {
                fun save(entity: E): String
                fun saveAll(entities: Iterable<E>): String
                fun saveList(entities: List<E>): String
                fun saveSet(entities: Set<E>): String
                fun saveSeq(entities: Sequence<E>): String
                fun pair(left: E, right: E): String
                fun pick(entity: E, mode: String = "AUTO"): String
            }

            interface MethodLevelRepository<E : Any> {
                @GenerateTransformOverloads
                fun only(entity: E): String

                fun untouched(entity: E): String
            }

            @GenerateTransformOverloads
            fun topSaveString(entity: String): String = entity
        """.trimIndent()
    }

    private fun repositoryUsageSource(): String {
        return """
            package org.babyfish.jimmer.spring.repo

            import org.babyfish.jimmer.AltInput
            import org.babyfish.jimmer.Draft
            import org.babyfish.jimmer.Input

            class StringInput(private val value: String) : Input<String> {
                override fun toEntity(): String = value
            }

            class StringDraft(private val value: String) : Draft<String> {
                override fun toEntity(): String = value
            }

            class StringAlt(private val value: String) : AltInput<String> {
                override fun unwrap(): String = value
            }

            fun use(
                repo: KotlinRepository<String>,
                methodRepo: MethodLevelRepository<String>,
                input: Input<String>,
                draft: Draft<String>,
                alt: AltInput<String>,
                inputs: Iterable<Input<String>>,
                drafts: Iterable<Draft<String>>,
                inputList: List<Input<String>>,
                inputSet: Set<Input<String>>,
                inputSeq: Sequence<Input<String>>,
            ) {
                repo.save(input)
                repo.save(draft)
                repo.save(alt)

                repo.saveAllViaToEntityInput(inputs)
                repo.saveAllViaFromDraft(drafts)
                repo.saveListViaToEntityInput(inputList)
                repo.saveSetViaToEntityInput(inputSet)
                repo.saveSeqViaToEntityInput(inputSeq)

                repo.pair(input, "right")
                repo.pair("left", input)
                repo.pair(input, input)

                repo.pick(input)
                methodRepo.only(input)
                topSaveStringViaToEntityInput(input)
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
                runtimeClasspath.forEach { path ->
                    add(File(path).toURI().toURL())
                }
            }.toTypedArray()
            val classLoader = URLClassLoader(urls, javaClass.classLoader)
            return classLoader.loadClass(name)
        }

        fun hasDeclaredMethodInPackage(
            packageName: String,
            predicate: (java.lang.reflect.Method) -> Boolean,
        ): Boolean {
            val urls = buildList {
                add(classesDir.toUri().toURL())
                runtimeClasspath.forEach { path ->
                    add(File(path).toURI().toURL())
                }
            }.toTypedArray()
            val packagePath = packageName.replace('.', File.separatorChar)
            URLClassLoader(urls, javaClass.classLoader).use { classLoader ->
                Files.walk(classesDir).use { paths ->
                    val iterator = paths.iterator()
                    while (iterator.hasNext()) {
                        val path = iterator.next()
                        if (!Files.isRegularFile(path) || !path.toString().endsWith(".class")) {
                            continue
                        }
                        val relativePath = classesDir.relativize(path).toString()
                        if (!relativePath.startsWith(packagePath)) {
                            continue
                        }
                        val className = relativePath
                            .removeSuffix(".class")
                            .replace(File.separatorChar, '.')
                        val klass = classLoader.loadClass(className)
                        if (klass.declaredMethods.any(predicate)) {
                            return true
                        }
                    }
                }
            }
            return false
        }
    }
}

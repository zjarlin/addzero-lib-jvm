package site.addzero.springktor.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import java.io.OutputStream

class SpringKtorGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {
    fun generate(model: SpringKtorModel, generatedPackage: String) {
        generateTopLevelRoutes(model.topLevelRoutes, generatedPackage)
        generateControllerRoutes(model.controllerRoutes, generatedPackage)
        generateAggregateFile(model, generatedPackage)
    }

    private fun generateTopLevelRoutes(routes: Set<TopLevelRouteMeta>, generatedPackage: String) {
        routes.groupBy { it.fileName }.values.forEach { fileRoutes ->
            val meta = fileRoutes.first()
            val functionName = "register${meta.fileName.toGeneratedTypeName()}SpringRoutes"
            val sourceFile = meta.sourceFilePath

            createFile(
                packageName = generatedPackage,
                fileName = functionName,
                sourceFilePaths = setOf(sourceFile),
            ).use { stream ->
                stream.write(
                    buildTopLevelRoutesFile(
                        generatedPackage = generatedPackage,
                        functionName = functionName,
                        routes = fileRoutes.sortedWith(compareBy({ it.path }, { it.functionName })),
                    ).toByteArray()
                )
            }
        }
    }

    private fun generateControllerRoutes(routes: Set<ControllerRouteMeta>, generatedPackage: String) {
        routes.groupBy { it.controllerQualifiedName }.values.forEach { controllerRoutes ->
            val meta = controllerRoutes.first()
            val functionName = "register${meta.controllerSimpleName.toGeneratedTypeName()}SpringRoutes"
            val sourceFile = meta.sourceFilePath

            createFile(
                packageName = generatedPackage,
                fileName = functionName,
                sourceFilePaths = setOf(sourceFile),
            ).use { stream ->
                stream.write(
                    buildControllerRoutesFile(
                        generatedPackage = generatedPackage,
                        functionName = functionName,
                        routes = controllerRoutes.sortedWith(compareBy({ it.path }, { it.functionName })),
                    ).toByteArray()
                )
            }
        }
    }

    private fun generateAggregateFile(model: SpringKtorModel, generatedPackage: String) {
        val sourcePaths = buildSet {
            addAll(model.topLevelRoutes.map { it.sourceFilePath })
            addAll(model.controllerRoutes.map { it.sourceFilePath })
            addAll(model.beanClasses.map { it.sourceFilePath })
            addAll(model.beanFactories.map { it.sourceFilePath })
        }

        createFile(
            packageName = generatedPackage,
            fileName = "GeneratedSpringApplication",
            sourceFilePaths = sourcePaths,
        ).use { stream ->
            stream.write(buildAggregateFile(model, generatedPackage).toByteArray())
        }
    }

    private fun buildTopLevelRoutesFile(
        generatedPackage: String,
        functionName: String,
        routes: List<TopLevelRouteMeta>,
    ): String {
        val routeBlocks = routes.joinToString("\n\n") { route ->
            buildRouteBlock(
                httpMethod = route.httpMethod,
                path = route.path,
                parameters = route.parameters,
                invocation = "${route.functionQualifiedName}(${route.parameters.joinToString(", ") { it.localName() }})",
                returnsUnit = route.returnsUnit,
                returnTypeName = route.returnTypeName,
            )
        }

        return """
            package $generatedPackage

            import io.ktor.server.routing.Route
            import io.ktor.server.routing.*
            import io.ktor.util.reflect.typeInfo
            import site.addzero.springktor.runtime.*

            fun Route.$functionName() {
            ${routeBlocks.prependIndent("    ")}
            }
        """.trimIndent()
    }

    private fun buildControllerRoutesFile(
        generatedPackage: String,
        functionName: String,
        routes: List<ControllerRouteMeta>,
    ): String {
        val controllerType = routes.first().controllerQualifiedName
        val eagerRouteBlocks = routes.joinToString("\n\n") { route ->
            buildRouteBlock(
                httpMethod = route.httpMethod,
                path = route.path,
                parameters = route.parameters,
                invocation = "controller.${route.functionName}(${route.parameters.joinToString(", ") { it.localName() }})",
                returnsUnit = route.returnsUnit,
                returnTypeName = route.returnTypeName,
            )
        }
        val lazyRouteBlocks = routes.joinToString("\n\n") { route ->
            buildRouteBlock(
                httpMethod = route.httpMethod,
                path = route.path,
                parameters = route.parameters,
                invocation = "call.resolveGeneratedSpringBean<$controllerType>().${route.functionName}(${route.parameters.joinToString(", ") { it.localName() }})",
                returnsUnit = route.returnsUnit,
                returnTypeName = route.returnTypeName,
            )
        }

        return """
            package $generatedPackage

            import io.ktor.server.routing.Route
            import io.ktor.server.routing.*
            import io.ktor.util.reflect.typeInfo
            import site.addzero.springktor.runtime.*

            fun Route.$functionName(controller: $controllerType) {
            ${eagerRouteBlocks.prependIndent("    ")}
            }

            fun Route.$functionName() {
            ${lazyRouteBlocks.prependIndent("    ")}
            }
        """.trimIndent()
    }

    private fun buildRouteBlock(
        httpMethod: SpringHttpMethod,
        path: String,
        parameters: List<ParameterMeta>,
        invocation: String,
        returnsUnit: Boolean,
        returnTypeName: String?,
    ): String {
        val multipartNeeded = parameters.any {
            it.bindingKind == ParameterBindingKind.REQUEST_PART_VALUE ||
                it.bindingKind == ParameterBindingKind.MULTIPART_FILE ||
                it.bindingKind == ParameterBindingKind.MULTIPART_FILE_LIST
        }
        val multipartLine = if (multipartNeeded) {
            "val _springMultipart = call.receiveSpringMultipartParts()"
        } else {
            null
        }
        val bindingLines = parameters.joinToString("\n") { parameter ->
            "val ${parameter.localName()} = ${parameter.bindingExpression()}"
        }
        val invocationLines = if (returnsUnit) {
            """
                $invocation
                call.completeSpringRoute(returnsUnit = true)
            """.trimIndent()
        } else {
            """
                val _springResult = $invocation
                call.completeSpringRoute(
                    result = _springResult,
                    resultType = typeInfo<$returnTypeName>(),
                )
            """.trimIndent()
        }

        val routeFunction = httpMethod.toRouteFunctionName()
        val bodyLines = listOfNotNull(multipartLine, bindingLines.takeIf { it.isNotBlank() }, invocationLines)
            .joinToString("\n")

        return """
            $routeFunction("${path.escapeKotlinString()}") {
                $bodyLines
            }
        """.trimIndent()
    }

    private fun buildAggregateFile(model: SpringKtorModel, generatedPackage: String): String {
        val topLevelCalls = model.topLevelRoutes
            .map { "register${it.fileName.toGeneratedTypeName()}SpringRoutes()" }
            .distinct()
            .sorted()
        val controllerCalls = model.controllerRoutes
            .map {
                "register${it.controllerSimpleName.toGeneratedTypeName()}SpringRoutes()"
            }
            .distinct()
            .sorted()

        val beanLines = buildList {
            model.beanClasses.sortedBy { it.qualifiedName }.forEach { beanClass ->
                val line = when {
                    beanClass.objectDeclaration -> "single { ${beanClass.qualifiedName} }"
                    beanClass.dependencyCount == 0 -> "single { ${beanClass.qualifiedName}() }"
                    else -> "single { ${beanClass.qualifiedName}(${beanClass.dependencyInvocation()}) }"
                }
                add(line)
            }
            model.beanFactories.sortedBy { "${it.configurationQualifiedName}.${it.methodName}" }.forEach { factory ->
                val invocation = if (factory.dependencyCount == 0) {
                    "get<${factory.configurationQualifiedName}>().${factory.methodName}()"
                } else {
                    "get<${factory.configurationQualifiedName}>().${factory.methodName}(${factory.dependencyInvocation()})"
                }
                add("single { $invocation }")
            }
        }

        val routeRegistrationLines = (topLevelCalls + controllerCalls).joinToString("\n") { it }

        return """
            package $generatedPackage

            import io.ktor.server.application.Application
            import io.ktor.server.routing.Route
            import io.ktor.server.routing.routing
            import org.koin.core.module.Module
            import org.koin.dsl.module
            import site.addzero.springktor.runtime.installOrLoadGeneratedSpringModule

            val generatedSpringKoinModule: Module = module {
            ${beanLines.joinToString("\n").prependIndent("    ")}
            }

            fun Route.registerGeneratedSpringRoutes() {
            ${routeRegistrationLines.prependIndent("    ")}
            }

            fun Application.generatedSpringApplication() {
                installOrLoadGeneratedSpringModule(generatedSpringKoinModule)
                routing {
                    registerGeneratedSpringRoutes()
                }
            }
        """.trimIndent()
    }

    private fun ParameterMeta.bindingExpression(): String {
        return when (bindingKind) {
            ParameterBindingKind.APPLICATION_CALL -> "call"
            ParameterBindingKind.APPLICATION -> "call.application"
            ParameterBindingKind.ROUTING_CONTEXT -> "this"
            ParameterBindingKind.APPLICATION_REQUEST -> "call.request"
            ParameterBindingKind.APPLICATION_RESPONSE -> "call.response"
            ParameterBindingKind.PATH_VARIABLE -> nullableCall("call.optionalPathVariable", "call.requirePathVariable")
            ParameterBindingKind.REQUEST_PARAM -> nullableCall("call.optionalRequestParam", "call.requireRequestParam")
            ParameterBindingKind.REQUEST_BODY -> nullableCall("call.optionalRequestBody", "call.requireRequestBody", bodyBinding = true)
            ParameterBindingKind.REQUEST_HEADER -> nullableCall("call.optionalRequestHeader", "call.requireRequestHeader")
            ParameterBindingKind.REQUEST_PART_VALUE -> nullableCall("_springMultipart.optionalValue", "_springMultipart.requireValue")
            ParameterBindingKind.MULTIPART_FILE -> if (nullable) {
                "_springMultipart.optionalFile(\"${externalName.escapeKotlinString()}\")"
            } else {
                "_springMultipart.requireFile(\"${externalName.escapeKotlinString()}\")"
            }

            ParameterBindingKind.MULTIPART_FILE_LIST -> if (nullable) {
                "_springMultipart.optionalFiles(\"${externalName.escapeKotlinString()}\")"
            } else {
                "_springMultipart.requireFiles(\"${externalName.escapeKotlinString()}\")"
            }
        }
    }

    private fun ParameterMeta.nullableCall(optionalCall: String, requiredCall: String, bodyBinding: Boolean = false): String {
        return if (bodyBinding) {
            if (nullable) {
                "$optionalCall<$nonNullTypeName>()"
            } else {
                "$requiredCall<$typeName>()"
            }
        } else if (nullable) {
            "$optionalCall<$nonNullTypeName>(\"${externalName.escapeKotlinString()}\")"
        } else {
            "$requiredCall<$typeName>(\"${externalName.escapeKotlinString()}\")"
        }
    }

    private fun ParameterMeta.localName(): String {
        return "_springArg$index"
    }

    private fun BeanClassMeta.dependencyInvocation(): String {
        return List(dependencyCount) { "get()" }.joinToString(", ")
    }

    private fun BeanFactoryMeta.dependencyInvocation(): String {
        return List(dependencyCount) { "get()" }.joinToString(", ")
    }

    private fun SpringHttpMethod.toRouteFunctionName(): String {
        return when (this) {
            SpringHttpMethod.GET -> "get"
            SpringHttpMethod.POST -> "post"
            SpringHttpMethod.PUT -> "put"
            SpringHttpMethod.DELETE -> "delete"
            SpringHttpMethod.PATCH -> "patch"
        }
    }

    private fun String.toGeneratedTypeName(): String {
        val pieces = split(Regex("[^A-Za-z0-9]+"))
            .filter { it.isNotBlank() }
            .map { part -> part.replaceFirstChar { it.uppercase() } }
        val joined = pieces.joinToString("")
        return if (joined.firstOrNull()?.isDigit() == true) {
            "Generated$joined"
        } else {
            joined.ifBlank { "Generated" }
        }
    }

    private fun String.escapeKotlinString(): String {
        return replace("\\", "\\\\").replace("\"", "\\\"")
    }

    private fun createFile(packageName: String, fileName: String, sourceFilePaths: Set<String>): OutputStream {
        logger.info("[SpringKtor] Generating $packageName.$fileName from ${sourceFilePaths.joinToString()}")
        return codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = packageName,
            fileName = fileName,
        )
    }
}

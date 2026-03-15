package site.addzero.springktor.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import java.io.OutputStream

class SpringKtorGenerator(
    private val codeGenerator: CodeGenerator,
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

            createFile(
                packageName = generatedPackage,
                fileName = functionName,
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

            createFile(
                packageName = generatedPackage,
                fileName = functionName,
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
        createFile(
            packageName = generatedPackage,
            fileName = "GeneratedSpringRoutes",
        ).use { stream ->
            stream.write(buildAggregateFile(model, generatedPackage).toByteArray())
        }
    }

    private fun buildTopLevelRoutesFile(
        generatedPackage: String,
        functionName: String,
        routes: List<TopLevelRouteMeta>,
    ): String {
        val needsTypeInfo = routes.any { it.returnTypeName != null }
        val routeBlocks = routes.joinToString("\n\n") { route ->
            buildRouteBlock(
                httpMethod = route.httpMethod,
                path = route.path,
                parameters = route.parameters,
                invocation = "${route.functionQualifiedName}(${route.parameters.joinToString(", ") { it.localName() }})",
                returnTypeName = route.returnTypeName,
            )
        }

        return buildString {
            appendLine("package $generatedPackage")
            appendLine()
            appendLine("import io.ktor.server.routing.Route")
            appendLine("import io.ktor.server.routing.*")
            if (needsTypeInfo) {
                appendLine("import io.ktor.util.reflect.typeInfo")
            }
            appendLine("import site.addzero.springktor.runtime.*")
            appendLine()
            appendLine("fun Route.$functionName() {")
            if (routeBlocks.isNotBlank()) {
                appendLine(routeBlocks.prependIndent("    "))
            }
            append("}")
        }
    }

    private fun buildControllerRoutesFile(
        generatedPackage: String,
        functionName: String,
        routes: List<ControllerRouteMeta>,
    ): String {
        val controllerType = routes.first().controllerQualifiedName
        val needsTypeInfo = routes.any { it.returnTypeName != null }
        val eagerRouteBlocks = routes.joinToString("\n\n") { route ->
            buildRouteBlock(
                httpMethod = route.httpMethod,
                path = route.path,
                parameters = route.parameters,
                invocation = "controller.${route.functionName}(${route.parameters.joinToString(", ") { it.localName() }})",
                returnTypeName = route.returnTypeName,
            )
        }
        val lazyRouteBlocks = routes.joinToString("\n\n") { route ->
            buildRouteBlock(
                httpMethod = route.httpMethod,
                path = route.path,
                parameters = route.parameters,
                invocation = "org.koin.mp.KoinPlatform.getKoin().get<$controllerType>().${route.functionName}(${route.parameters.joinToString(", ") { it.localName() }})",
                returnTypeName = route.returnTypeName,
            )
        }

        return buildString {
            appendLine("package $generatedPackage")
            appendLine()
            appendLine("import io.ktor.server.routing.Route")
            appendLine("import io.ktor.server.routing.*")
            if (needsTypeInfo) {
                appendLine("import io.ktor.util.reflect.typeInfo")
            }
            appendLine("import site.addzero.springktor.runtime.*")
            appendLine()
            appendLine("fun Route.$functionName(controller: $controllerType) {")
            if (eagerRouteBlocks.isNotBlank()) {
                appendLine(eagerRouteBlocks.prependIndent("    "))
            }
            appendLine("}")
            appendLine()
            appendLine("fun Route.$functionName() {")
            if (lazyRouteBlocks.isNotBlank()) {
                appendLine(lazyRouteBlocks.prependIndent("    "))
            }
            append("}")
        }
    }

    private fun buildRouteBlock(
        httpMethod: SpringHttpMethod,
        path: String,
        parameters: List<ParameterMeta>,
        invocation: String,
        returnTypeName: String?,
    ): String {
        val multipartNeeded = parameters.any {
            it.bindingKind == ParameterBindingKind.REQUEST_PART_VALUE ||
                it.bindingKind == ParameterBindingKind.MULTIPART_FILE ||
                it.bindingKind == ParameterBindingKind.MULTIPART_FILE_LIST
        }
        val multipartLine = if (multipartNeeded) {
            "val _multipart = call.receiveMultipartParts()"
        } else {
            null
        }
        val bindingLines = parameters.joinToString("\n") { parameter ->
            "val ${parameter.localName()} = ${parameter.bindingExpression()}"
        }
        val responseLine = if (returnTypeName == null) {
            "call.respondGeneratedRouteResult(result = _routeResult)"
        } else {
            """
                call.respondGeneratedRouteResult(
                    result = _routeResult,
                    resultType = typeInfo<$returnTypeName>(),
                )
            """.trimIndent()
        }
        val invocationLines = """
            val _routeResult = $invocation
            $responseLine
        """.trimIndent()

        val routeFunction = httpMethod.toRouteFunctionName()
        val bodyLines = listOfNotNull(multipartLine, bindingLines.takeIf { it.isNotBlank() }, invocationLines)
            .joinToString("\n")
            .prependIndent("    ")

        return buildString {
            appendLine("""$routeFunction("${path.escapeKotlinString()}") {""")
            appendLine(bodyLines)
            append("}")
        }
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

        val routeRegistrationLines = (topLevelCalls + controllerCalls).joinToString("\n")

        return buildString {
            appendLine("package $generatedPackage")
            appendLine()
            appendLine("import io.ktor.server.routing.Route")
            appendLine()
            appendLine("fun Route.registerGeneratedSpringRoutes() {")
            if (routeRegistrationLines.isNotBlank()) {
                appendLine(routeRegistrationLines.prependIndent("    "))
            }
            append("}")
        }
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
            ParameterBindingKind.REQUEST_PART_VALUE -> nullableCall("_multipart.optionalValue", "_multipart.requireValue")
            ParameterBindingKind.MULTIPART_FILE -> if (nullable) {
                "_multipart.optionalFile(\"${externalName.escapeKotlinString()}\")"
            } else {
                "_multipart.requireFile(\"${externalName.escapeKotlinString()}\")"
            }

            ParameterBindingKind.MULTIPART_FILE_LIST -> if (nullable) {
                "_multipart.optionalFiles(\"${externalName.escapeKotlinString()}\")"
            } else {
                "_multipart.requireFiles(\"${externalName.escapeKotlinString()}\")"
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
        return "_arg$index"
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

    private fun createFile(packageName: String, fileName: String): OutputStream {
        return codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = packageName,
            fileName = fileName,
        )
    }
}

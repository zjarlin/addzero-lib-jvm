package site.addzero.ksp.metadata.openapi.ktor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import java.io.File
import site.addzero.ksp.metadata.openapi.model.OpenApiEndpointMeta
import site.addzero.ksp.metadata.openapi.model.OpenApiExtractionWarning
import site.addzero.ksp.metadata.openapi.model.OpenApiFramework
import site.addzero.ksp.metadata.openapi.model.OpenApiMetadataBundle
import site.addzero.ksp.metadata.openapi.model.OpenApiOwnerKind
import site.addzero.ksp.metadata.openapi.model.OpenApiOwnerMeta

private const val ROUTE = "io.ktor.server.routing.Route"
private const val ROUTING = "io.ktor.server.routing.Routing"
private const val APPLICATION = "io.ktor.server.application.Application"

data class KtorRouteOpenApiExtractionResult(
    val metadata: OpenApiMetadataBundle,
    val deferred: List<KSAnnotated>,
)

object KtorRouteOpenApiMetadata {
    fun extract(
        resolver: Resolver,
        logger: KSPLogger? = null,
    ): KtorRouteOpenApiExtractionResult {
        return KtorRouteOpenApiMetadataExtractor(resolver, logger).extract()
    }
}

class KtorRouteOpenApiMetadataExtractor(
    private val resolver: Resolver,
    private val logger: KSPLogger? = null,
) {
    private val deferred = linkedSetOf<KSAnnotated>()
    private val warnings = mutableListOf<OpenApiExtractionWarning>()
    private val endpoints = linkedMapOf<String, OpenApiEndpointMeta>()
    private val loadedSources = linkedMapOf<String, KtorSourceText>()

    fun extract(): KtorRouteOpenApiExtractionResult {
        for (file in resolver.getAllFiles()) {
            val source = loadSource(file) ?: continue
            for (function in file.collectFunctionDeclarations()) {
                if (!function.validate()) {
                    deferred += function
                    continue
                }
                extractFromFunction(function, source)
            }
        }
        return KtorRouteOpenApiExtractionResult(
            metadata = OpenApiMetadataBundle(
                framework = OpenApiFramework.KTOR,
                endpoints = endpoints.values.sortedWith(compareBy({ it.path }, { it.httpMethod?.ordinal ?: Int.MAX_VALUE }, { it.operationId })),
                warnings = warnings.toList(),
            ),
            deferred = deferred.toList(),
        )
    }

    private fun extractFromFunction(function: KSFunctionDeclaration, source: KtorSourceText) {
        val receiver = function.extensionReceiver?.resolve()?.declaration?.qualifiedName?.asString()
        val bodyRange = findFunctionBody(function, source) ?: return
        val bodyText = source.text.substring(bodyRange.bodyStart, bodyRange.bodyEnd)
        if (!bodyText.contains("routing") &&
            !bodyText.contains("route(") &&
            !bodyText.contains("get(") &&
            !bodyText.contains("post(") &&
            !bodyText.contains("put(") &&
            !bodyText.contains("delete(") &&
            !bodyText.contains("patch(") &&
            !bodyText.contains("webSocket") &&
            !bodyText.contains("sse(")
        ) {
            return
        }

        val onlyRoutingBlocks = receiver == APPLICATION || receiver == null
        if (receiver != null && receiver !in setOf(ROUTE, ROUTING, APPLICATION)) {
            return
        }

        val parser = KtorRoutingSourceParser(source) { message, line ->
            warn(message, function, source.path, line)
        }
        val routes = parser.parse(
            bodyText = bodyText,
            bodyAbsoluteStart = bodyRange.bodyStart,
            onlyRoutingBlocks = onlyRoutingBlocks,
        )
        if (routes.isEmpty()) {
            return
        }

        val owner = buildOwner(function)
        val summary = extractSummary(function.docString)
        val description = function.docString?.trim()?.ifBlank { null }
        val tags = buildTags(function)
        routes.forEach { route ->
            val operationId = buildOperationId(function, route.path, route.absoluteOffset)
            endpoints[operationId] = OpenApiEndpointMeta(
                framework = OpenApiFramework.KTOR,
                owner = owner,
                routeKind = route.routeKind,
                httpMethod = route.httpMethod,
                path = route.path,
                functionName = function.simpleName.asString(),
                operationId = operationId,
                summary = summary,
                description = description,
                tags = tags,
                isSuspend = Modifier.SUSPEND in function.modifiers,
                sourceLine = source.lineNumberOf(route.absoluteOffset),
            )
        }
    }

    private fun buildOwner(function: KSFunctionDeclaration): OpenApiOwnerMeta {
        val parent = function.parentDeclaration as? KSClassDeclaration
        return if (parent == null) {
            OpenApiOwnerMeta(
                kind = OpenApiOwnerKind.ROUTE_FUNCTION,
                packageName = function.packageName.asString(),
                simpleName = function.simpleName.asString(),
                qualifiedName = function.qualifiedName?.asString(),
                sourceFile = function.containingFile?.filePath,
            )
        } else {
            OpenApiOwnerMeta(
                kind = OpenApiOwnerKind.ROUTE_FUNCTION,
                packageName = parent.packageName.asString(),
                simpleName = function.simpleName.asString(),
                qualifiedName = function.qualifiedName?.asString() ?: parent.qualifiedName?.asString(),
                sourceFile = function.containingFile?.filePath,
            )
        }
    }

    private fun buildTags(function: KSFunctionDeclaration): List<String> {
        val parent = function.parentDeclaration as? KSClassDeclaration
        if (parent != null) {
            return listOf(parent.simpleName.asString())
        }
        val fileName = function.containingFile?.fileName?.removeSuffix(".kt")
        return listOf(fileName ?: function.simpleName.asString())
    }

    private fun extractSummary(docString: String?): String? {
        return docString?.lineSequence()?.map { it.trim() }?.firstOrNull { it.isNotBlank() }
    }

    private fun buildOperationId(function: KSFunctionDeclaration, path: String, absoluteOffset: Int): String {
        val base = function.qualifiedName?.asString() ?: "${function.packageName.asString()}.${function.simpleName.asString()}"
        val pathToken = path.trim('/').replace("/", "_").replace("{", "").replace("}", "").ifBlank { "root" }
        return "$base.$pathToken.${absoluteOffset}"
    }

    private fun findFunctionBody(function: KSFunctionDeclaration, source: KtorSourceText): FunctionBodyRange? {
        val location = function.location as? FileLocation ?: return null
        val startIndex = source.lineStart(location.lineNumber)
        val signatureRegex = Regex("\\bfun\\b[^\\n\\r{=]*\\b${Regex.escape(function.simpleName.asString())}\\b")
        val match = signatureRegex.find(source.text, startIndex) ?: return null
        var index = match.range.last + 1
        while (index < source.text.length) {
            index = skipWhitespaceAndComments(source.text, index)
            if (index >= source.text.length || source.text[index] == '=') {
                return null
            }
            if (source.text[index] == '{') {
                val end = findMatchingBrace(source.text, index) ?: return null
                return FunctionBodyRange(index + 1, end)
            }
            index++
        }
        return null
    }

    private fun loadSource(file: KSFile): KtorSourceText? {
        val path = file.filePath
        if (path.isNullOrBlank()) {
            warn("Unable to resolve source path for Ktor file `${file.fileName}`.", file = file)
            return null
        }
        return loadedSources.getOrPut(path) {
            val text = File(path).readText()
            val lineStarts = ArrayList<Int>().apply {
                add(0)
                for (index in text.indices) {
                    if (text[index] == '\n' && index + 1 < text.length) {
                        add(index + 1)
                    }
                }
            }.toIntArray()
            KtorSourceText(path, text, lineStarts)
        }
    }

    private fun skipWhitespaceAndComments(text: String, startIndex: Int): Int {
        var index = startIndex
        while (index < text.length) {
            index = when {
                text.startsWith("//", index) -> {
                    var cursor = index + 2
                    while (cursor < text.length && text[cursor] != '\n') cursor++
                    cursor
                }

                text.startsWith("/*", index) -> {
                    val end = text.indexOf("*/", index + 2)
                    if (end == -1) text.length else end + 2
                }

                text[index].isWhitespace() -> index + 1
                else -> return index
            }
        }
        return index
    }

    private fun findMatchingBrace(text: String, startIndex: Int): Int? {
        var index = startIndex
        var depth = 0
        while (index < text.length) {
            when {
                text.startsWith("//", index) -> {
                    while (index < text.length && text[index] != '\n') index++
                }

                text.startsWith("/*", index) -> {
                    val end = text.indexOf("*/", index + 2)
                    index = if (end == -1) text.length else end + 2
                }

                text.startsWith("\"\"\"", index) -> {
                    val end = text.indexOf("\"\"\"", index + 3)
                    index = if (end == -1) text.length else end + 3
                }

                text[index] == '"' -> {
                    index++
                    while (index < text.length) {
                        if (text[index] == '\\') {
                            index += 2
                            continue
                        }
                        if (text[index] == '"') {
                            index++
                            break
                        }
                        index++
                    }
                }

                text[index] == '\'' -> {
                    index++
                    while (index < text.length) {
                        if (text[index] == '\\') {
                            index += 2
                            continue
                        }
                        if (text[index] == '\'') {
                            index++
                            break
                        }
                        index++
                    }
                }

                text[index] == '{' -> {
                    depth++
                    index++
                }

                text[index] == '}' -> {
                    depth--
                    if (depth == 0) {
                        return index
                    }
                    index++
                }

                else -> index++
            }
        }
        return null
    }

    private fun KSFile.collectFunctionDeclarations(): List<KSFunctionDeclaration> {
        val results = mutableListOf<KSFunctionDeclaration>()
        fun visit(declaration: KSDeclaration) {
            when (declaration) {
                is KSFunctionDeclaration -> results += declaration
                is KSClassDeclaration -> declaration.declarations.forEach(::visit)
            }
        }
        declarations.forEach(::visit)
        return results
    }

    private fun warn(
        message: String,
        symbol: KSAnnotated? = null,
        sourceFile: String? = null,
        line: Int? = null,
        file: KSFile? = null,
    ) {
        warnings += OpenApiExtractionWarning(
            framework = OpenApiFramework.KTOR,
            message = message,
            sourceFile = sourceFile ?: (symbol as? KSDeclaration)?.containingFile?.filePath ?: file?.filePath,
            ownerQualifiedName = (symbol as? KSDeclaration)?.qualifiedName?.asString(),
            line = line,
        )
        if (symbol == null) logger?.warn(message) else logger?.warn(message, symbol)
    }

    private data class FunctionBodyRange(
        val bodyStart: Int,
        val bodyEnd: Int,
    )
}

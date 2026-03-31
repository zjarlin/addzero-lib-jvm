package site.addzero.ksp.metadata.openapi.ktor

import site.addzero.ksp.metadata.openapi.model.OpenApiHttpMethod
import site.addzero.ksp.metadata.openapi.model.OpenApiRouteKind

internal data class KtorSourceText(
    val path: String,
    val text: String,
    val lineStarts: IntArray,
) {
    fun lineStart(lineNumber: Int): Int {
        if (lineNumber <= 1) {
            return 0
        }
        return lineStarts.getOrElse(lineNumber - 1) { 0 }
    }

    fun lineNumberOf(offset: Int): Int {
        var low = 0
        var high = lineStarts.size - 1
        while (low <= high) {
            val mid = (low + high) ushr 1
            val start = lineStarts[mid]
            val next = if (mid + 1 < lineStarts.size) lineStarts[mid + 1] else Int.MAX_VALUE
            when {
                offset < start -> high = mid - 1
                offset >= next -> low = mid + 1
                else -> return mid + 1
            }
        }
        return 1
    }
}

internal data class KtorDiscoveredRoute(
    val routeKind: OpenApiRouteKind,
    val httpMethod: OpenApiHttpMethod?,
    val path: String,
    val absoluteOffset: Int,
)

internal class KtorRoutingSourceParser(
    private val source: KtorSourceText,
    private val warn: (message: String, line: Int?) -> Unit,
) {
    private val discoveredRoutes = mutableListOf<KtorDiscoveredRoute>()

    fun parse(
        bodyText: String,
        bodyAbsoluteStart: Int,
        currentPath: String = "/",
        onlyRoutingBlocks: Boolean = false,
    ): List<KtorDiscoveredRoute> {
        parseRoutingBlock(
            segment = bodyText,
            absoluteStart = bodyAbsoluteStart,
            currentPath = currentPath,
            onlyRoutingBlocks = onlyRoutingBlocks,
        )
        return discoveredRoutes.toList()
    }

    private fun parseRoutingBlock(
        segment: String,
        absoluteStart: Int,
        currentPath: String,
        onlyRoutingBlocks: Boolean,
    ) {
        var index = 0
        while (index < segment.length) {
            index = when {
                segment.startsWith("//", index) -> skipLineComment(segment, index)
                segment.startsWith("/*", index) -> skipBlockComment(segment, index)
                segment.startsWith("\"\"\"", index) -> skipTripleQuotedString(segment, index)
                segment[index] == '"' -> skipQuotedString(segment, index, '"')
                segment[index] == '\'' -> skipQuotedString(segment, index, '\'')
                segment[index].isIdentifierStart() -> {
                    val nameStart = index
                    val nameEnd = readIdentifierEnd(segment, index)
                    val name = segment.substring(index, nameEnd)
                    if (name !in supportedCallNames) {
                        nameEnd
                    } else {
                        val parsedCall = parseCall(segment, nameEnd)
                        if (parsedCall == null) {
                            nameEnd
                        } else if (parsedCall.blockStart == null || parsedCall.blockEnd == null) {
                            parsedCall.nextIndex
                        } else {
                            handleCall(
                                name = name,
                                arguments = parsedCall.arguments,
                                block = segment.substring(parsedCall.blockStart + 1, parsedCall.blockEnd),
                                callAbsoluteOffset = absoluteStart + nameStart,
                                blockAbsoluteStart = absoluteStart + parsedCall.blockStart + 1,
                                currentPath = currentPath,
                                onlyRoutingBlocks = onlyRoutingBlocks,
                            )
                            parsedCall.nextIndex
                        }
                    }
                }

                else -> index + 1
            }
        }
    }

    private fun handleCall(
        name: String,
        arguments: String?,
        block: String,
        callAbsoluteOffset: Int,
        blockAbsoluteStart: Int,
        currentPath: String,
        onlyRoutingBlocks: Boolean,
    ) {
        if (onlyRoutingBlocks && name != "routing") {
            return
        }

        when {
            name in transparentContainerCalls -> {
                parseRoutingBlock(block, blockAbsoluteStart, currentPath, false)
            }

            name in routeContainerCalls -> {
                val pathLiteral = extractLiteralPath(arguments)
                if (pathLiteral == null && !arguments.isNullOrBlank()) {
                    warn(
                        "Ktor route container `$name` uses a non-literal path; nested routes keep the current prefix.",
                        source.lineNumberOf(callAbsoluteOffset),
                    )
                }
                parseRoutingBlock(block, blockAbsoluteStart, combinePaths(currentPath, pathLiteral.orEmpty()), false)
            }

            name in httpCallNames.keys -> {
                val pathLiteral = extractLiteralPath(arguments)
                if (pathLiteral == null && !arguments.isNullOrBlank()) {
                    warn(
                        "Ktor HTTP route `$name` uses a non-literal path and is skipped.",
                        source.lineNumberOf(callAbsoluteOffset),
                    )
                    return
                }
                discoveredRoutes += KtorDiscoveredRoute(
                    routeKind = OpenApiRouteKind.HTTP,
                    httpMethod = httpCallNames.getValue(name),
                    path = combinePaths(currentPath, pathLiteral.orEmpty()),
                    absoluteOffset = callAbsoluteOffset,
                )
            }

            name in websocketCalls -> {
                val pathLiteral = extractLiteralPath(arguments)
                if (pathLiteral == null && !arguments.isNullOrBlank()) {
                    warn(
                        "Ktor websocket route `$name` uses a non-literal path and is skipped.",
                        source.lineNumberOf(callAbsoluteOffset),
                    )
                    return
                }
                discoveredRoutes += KtorDiscoveredRoute(
                    routeKind = OpenApiRouteKind.WEBSOCKET,
                    httpMethod = null,
                    path = combinePaths(currentPath, pathLiteral.orEmpty()),
                    absoluteOffset = callAbsoluteOffset,
                )
            }

            name in sseCalls -> {
                val pathLiteral = extractLiteralPath(arguments)
                if (pathLiteral == null && !arguments.isNullOrBlank()) {
                    warn(
                        "Ktor SSE route `$name` uses a non-literal path and is skipped.",
                        source.lineNumberOf(callAbsoluteOffset),
                    )
                    return
                }
                discoveredRoutes += KtorDiscoveredRoute(
                    routeKind = OpenApiRouteKind.SSE,
                    httpMethod = OpenApiHttpMethod.GET,
                    path = combinePaths(currentPath, pathLiteral.orEmpty()),
                    absoluteOffset = callAbsoluteOffset,
                )
            }
        }
    }

    private fun parseCall(segment: String, nameEnd: Int): ParsedCall? {
        var cursor = skipWhitespace(segment, nameEnd)
        if (cursor < segment.length && segment[cursor] == '<') {
            val genericEnd = findMatchingSymbol(segment, cursor, '<', '>') ?: return null
            cursor = skipWhitespace(segment, genericEnd + 1)
        }
        var arguments: String? = null
        if (cursor < segment.length && segment[cursor] == '(') {
            val argumentsEnd = findMatchingSymbol(segment, cursor, '(', ')') ?: return null
            arguments = segment.substring(cursor + 1, argumentsEnd)
            cursor = skipWhitespace(segment, argumentsEnd + 1)
        }
        if (cursor >= segment.length || segment[cursor] != '{') {
            return ParsedCall(arguments, null, null, cursor.coerceAtLeast(nameEnd))
        }
        val blockEnd = findMatchingSymbol(segment, cursor, '{', '}') ?: return null
        return ParsedCall(arguments, cursor, blockEnd, blockEnd + 1)
    }

    private fun extractLiteralPath(arguments: String?): String? {
        if (arguments.isNullOrBlank()) {
            return ""
        }
        val matcher = literalStringRegex.find(arguments) ?: return null
        val value = matcher.groups[1]?.value ?: matcher.groups[2]?.value ?: return null
        if (value.contains("$")) {
            return null
        }
        return value
            .replace("\\\"", "\"")
            .replace("\\'", "'")
            .replace("\\\\", "\\")
    }

    private fun combinePaths(basePath: String, childPath: String): String {
        val normalizedBase = normalizePath(basePath)
        val normalizedChild = normalizePath(childPath)
        if (normalizedBase == "/" && normalizedChild == "/") return "/"
        if (normalizedBase == "/") return normalizedChild
        if (normalizedChild == "/") return normalizedBase
        return (normalizedBase.trimEnd('/') + "/" + normalizedChild.trimStart('/')).replace("//", "/")
    }

    private fun normalizePath(path: String): String {
        val trimmed = path.trim()
        if (trimmed.isBlank()) return "/"
        return (if (trimmed.startsWith('/')) trimmed else "/$trimmed").replace("//", "/")
    }

    private fun findMatchingSymbol(text: String, startIndex: Int, open: Char, close: Char): Int? {
        var index = startIndex
        var depth = 0
        while (index < text.length) {
            when {
                text.startsWith("//", index) -> index = skipLineComment(text, index)
                text.startsWith("/*", index) -> index = skipBlockComment(text, index)
                text.startsWith("\"\"\"", index) -> index = skipTripleQuotedString(text, index)
                text[index] == '"' -> index = skipQuotedString(text, index, '"')
                text[index] == '\'' -> index = skipQuotedString(text, index, '\'')
                text[index] == open -> {
                    depth++
                    index++
                }

                text[index] == close -> {
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

    private fun skipWhitespace(text: String, startIndex: Int): Int {
        var index = startIndex
        while (index < text.length && text[index].isWhitespace()) {
            index++
        }
        return index
    }

    private fun skipLineComment(text: String, startIndex: Int): Int {
        var index = startIndex + 2
        while (index < text.length && text[index] != '\n') {
            index++
        }
        return index
    }

    private fun skipBlockComment(text: String, startIndex: Int): Int {
        val end = text.indexOf("*/", startIndex + 2)
        return if (end == -1) text.length else end + 2
    }

    private fun skipTripleQuotedString(text: String, startIndex: Int): Int {
        val end = text.indexOf("\"\"\"", startIndex + 3)
        return if (end == -1) text.length else end + 3
    }

    private fun skipQuotedString(text: String, startIndex: Int, delimiter: Char): Int {
        var index = startIndex + 1
        while (index < text.length) {
            if (text[index] == '\\') {
                index += 2
                continue
            }
            if (text[index] == delimiter) {
                return index + 1
            }
            index++
        }
        return text.length
    }

    private fun readIdentifierEnd(text: String, startIndex: Int): Int {
        var index = startIndex
        while (index < text.length && text[index].isIdentifierPart()) {
            index++
        }
        return index
    }

    private fun Char.isIdentifierStart(): Boolean {
        return this == '_' || isLetter()
    }

    private fun Char.isIdentifierPart(): Boolean {
        return this == '_' || isLetterOrDigit()
    }

    private data class ParsedCall(
        val arguments: String?,
        val blockStart: Int?,
        val blockEnd: Int?,
        val nextIndex: Int,
    )

    companion object {
        private val httpCallNames = linkedMapOf(
            "get" to OpenApiHttpMethod.GET,
            "post" to OpenApiHttpMethod.POST,
            "put" to OpenApiHttpMethod.PUT,
            "delete" to OpenApiHttpMethod.DELETE,
            "patch" to OpenApiHttpMethod.PATCH,
            "head" to OpenApiHttpMethod.HEAD,
            "options" to OpenApiHttpMethod.OPTIONS,
            "trace" to OpenApiHttpMethod.TRACE,
        )

        private val transparentContainerCalls = setOf("routing", "authenticate")
        private val routeContainerCalls = setOf("route")
        private val websocketCalls = setOf("webSocket", "webSocketRaw")
        private val sseCalls = setOf("sse")
        private val supportedCallNames = httpCallNames.keys + transparentContainerCalls + routeContainerCalls + websocketCalls + sseCalls
        private val literalStringRegex = Regex("(?s)\"((?:\\\\.|[^\"\\\\])*)\"|'((?:\\\\.|[^'\\\\])*)'")
    }
}

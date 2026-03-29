package site.addzero.kcloud.codegen

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.parser.OpenAPIV3Parser
import site.addzero.kcloud.codegen.ir.*

/**
 * 解析 OpenAPI 规范文件，将其转换为内部中间表示 (IR)。
 */
class SchemaParser {

    /**
     * 解析指定路径的 OpenAPI 规范文件（JSON 或 YAML）。
     */
    fun parse(specPath: String): ParseResult {
        val result = OpenAPIV3Parser().readLocation(specPath, null, null)
        val openApi = result.openAPI
            ?: return ParseResult.Failure(
                result.messages?.takeIf { it.isNotEmpty() }
                    ?: listOf("Failed to parse OpenAPI spec at: $specPath")
            )

        // swagger-parser 可能返回 openAPI 但仍有 validation messages
        // 这里我们只在完全无法解析时才报错，有 warning 时继续

        return try {
            val interfaces = parseInterfaces(openApi)
            val models = parseModels(openApi)
            ParseResult.Success(OpenApiIR(interfaces, models))
        } catch (e: Exception) {
            ParseResult.Failure(listOf("Error converting OpenAPI to IR: ${e.message}"))
        }
    }

    /**
     * 解析 OpenAPI 内容字符串（用于测试等场景）。
     */
    fun parseContent(content: String): ParseResult {
        val result = OpenAPIV3Parser().readContents(content, null, null)
        val openApi = result.openAPI
            ?: return ParseResult.Failure(
                result.messages?.takeIf { it.isNotEmpty() }
                    ?: listOf("Failed to parse OpenAPI spec content")
            )

        return try {
            val interfaces = parseInterfaces(openApi)
            val models = parseModels(openApi)
            ParseResult.Success(OpenApiIR(interfaces, models))
        } catch (e: Exception) {
            ParseResult.Failure(listOf("Error converting OpenAPI to IR: ${e.message}"))
        }
    }

    // ── Interfaces (paths → operations grouped by tag) ──

    private fun parseInterfaces(openApi: OpenAPI): List<ApiInterfaceIR> {
        val paths = openApi.paths ?: return emptyList()

        // 收集所有 (tag, operation) 对
        val operationsByTag = mutableMapOf<String, MutableList<OperationIR>>()

        for ((path, pathItem) in paths) {
            for ((method, operation) in extractOperations(pathItem)) {
                val tag = operation.tags?.firstOrNull() ?: "DefaultApi"
                val operationIR = convertOperation(operation, method, path)
                operationsByTag.getOrPut(tag) { mutableListOf() }.add(operationIR)
            }
        }

        return operationsByTag.map { (tag, ops) ->
            ApiInterfaceIR(
                name = toInterfaceName(tag),
                operations = ops
            )
        }
    }

    private fun extractOperations(pathItem: PathItem): List<Pair<HttpMethod, Operation>> {
        val ops = mutableListOf<Pair<HttpMethod, Operation>>()
        pathItem.get?.let { ops.add(HttpMethod.GET to it) }
        pathItem.post?.let { ops.add(HttpMethod.POST to it) }
        pathItem.put?.let { ops.add(HttpMethod.PUT to it) }
        pathItem.delete?.let { ops.add(HttpMethod.DELETE to it) }
        pathItem.patch?.let { ops.add(HttpMethod.PATCH to it) }
        return ops
    }

    private fun convertOperation(
        operation: Operation,
        httpMethod: HttpMethod,
        path: String
    ): OperationIR {
        val functionName = if (!operation.operationId.isNullOrBlank()) {
            toCamelCase(operation.operationId)
        } else {
            generateFunctionName(httpMethod, path)
        }

        val parameters = (operation.parameters ?: emptyList()).mapNotNull { param ->
            val location = when (param.`in`) {
                "path" -> ParameterLocation.PATH
                "query" -> ParameterLocation.QUERY
                "header" -> ParameterLocation.HEADER
                else -> return@mapNotNull null
            }
            val schema = param.schema ?: return@mapNotNull null
            ParameterIR(
                name = param.name,
                location = location,
                type = TypeMapper.map(schema),
                required = param.required ?: (location == ParameterLocation.PATH)
            )
        }

        val requestBody = operation.requestBody?.content
            ?.get("application/json")?.schema
            ?.let { TypeMapper.map(it) }

        val responseType = extractResponseType(operation)

        return OperationIR(
            functionName = functionName,
            httpMethod = httpMethod,
            path = path,
            summary = operation.summary,
            parameters = parameters,
            requestBody = requestBody,
            responseType = responseType
        )
    }

    private fun extractResponseType(operation: Operation): TypeRef {
        val responses = operation.responses ?: return TypeRef.Unit
        // 优先查找 200，然后 201，然后第一个 2xx 响应
        val successResponse = responses["200"]
            ?: responses["201"]
            ?: responses.entries.firstOrNull { it.key.startsWith("2") }?.value
            ?: return TypeRef.Unit

        val schema = successResponse.content
            ?.get("application/json")?.schema
            ?: return TypeRef.Unit

        return TypeMapper.map(schema)
    }

    // ── Models (components/schemas → ModelIR) ──

    private fun parseModels(openApi: OpenAPI): List<ModelIR> {
        val schemas = openApi.components?.schemas ?: return emptyList()

        return schemas.map { (name, schema) ->
            convertModel(name, schema, openApi)
        }
    }

    private fun convertModel(name: String, schema: Schema<*>, openApi: OpenAPI? = null): ModelIR {
        val requiredProps = schema.required?.toSet() ?: emptySet()

        // 处理 allOf: 合并所有子 schema 的属性
        val properties = if (!schema.allOf.isNullOrEmpty()) {
            flattenAllOf(schema.allOf, requiredProps, openApi)
        } else if (!schema.oneOf.isNullOrEmpty() || !schema.anyOf.isNullOrEmpty()) {
            // oneOf/anyOf: 取第一个子 schema 的属性，或回退为空
            val firstSchema = (schema.oneOf ?: schema.anyOf)?.firstOrNull()
            if (firstSchema != null) {
                extractProperties(firstSchema, requiredProps)
            } else {
                emptyList()
            }
        } else {
            extractProperties(schema, requiredProps)
        }

        return ModelIR(
            name = name,
            properties = properties,
            description = schema.description
        )
    }

    private fun flattenAllOf(
        allOfSchemas: List<Schema<*>>,
        parentRequired: Set<String>,
        openApi: OpenAPI? = null
    ): List<PropertyIR> {
        val allProperties = mutableListOf<PropertyIR>()
        val seenNames = mutableSetOf<String>()

        for (subSchema in allOfSchemas) {
            val subRequired = subSchema.required?.toSet() ?: emptySet()
            val combinedRequired = parentRequired + subRequired

            // 处理 $ref 引用：swagger-parser 在 readContents 中可能不完全解析 $ref
            val resolvedSchema = if (subSchema.properties.isNullOrEmpty() && subSchema.`$ref` != null) {
                val refName = subSchema.`$ref`.substringAfterLast("/")
                openApi?.components?.schemas?.get(refName) ?: subSchema
            } else {
                subSchema
            }

            // 递归处理嵌套的 allOf
            val props = if (!resolvedSchema.allOf.isNullOrEmpty()) {
                flattenAllOf(resolvedSchema.allOf, combinedRequired, openApi)
            } else {
                extractProperties(resolvedSchema, combinedRequired)
            }

            for (prop in props) {
                if (seenNames.add(prop.name)) {
                    allProperties.add(prop)
                }
            }
        }

        return allProperties
    }

    private fun extractProperties(
        schema: Schema<*>,
        requiredProps: Set<String>
    ): List<PropertyIR> {
        val properties = schema.properties ?: return emptyList()

        return properties.map { (propName, propSchema) ->
            val isRequired = propName in requiredProps
            PropertyIR(
                name = propName,
                type = TypeMapper.map(propSchema),
                required = isRequired,
                defaultValue = propSchema.default?.toString(),
                description = propSchema.description
            )
        }
    }

    // ── Naming helpers ──

    /**
     * 将 tag 名转换为接口名（PascalCase + "Api" 后缀）。
     * 例如: "favorites" → "FavoritesApi", "suno" → "SunoApi"
     */
    private fun toInterfaceName(tag: String): String {
        if (tag.endsWith("Api", ignoreCase = true)) {
            return toPascalCase(tag)
        }
        return toPascalCase(tag) + "Api"
    }

    /**
     * 从 HTTP 方法和路径生成函数名。
     * 例如: GET /api/favorites/{trackId} → "getApiFavoritesByTrackId"
     */
    private fun generateFunctionName(method: HttpMethod, path: String): String {
        val segments = path.split("/")
            .filter { it.isNotBlank() }
            .joinToString("_") { segment ->
                if (segment.startsWith("{") && segment.endsWith("}")) {
                    "by_" + segment.removeSurrounding("{", "}")
                } else {
                    segment
                }
            }
        return toCamelCase(method.name.lowercase() + "_" + segments)
    }

    companion object {
        /**
         * 将字符串转换为 camelCase。
         * 支持分隔符: '_', '-', ' ', '.'
         * 例如: "get_user_info" → "getUserInfo", "GetUserInfo" → "getUserInfo"
         */
        internal fun toCamelCase(input: String): String {
            if (input.isBlank()) return input

            val parts = input.split(Regex("[_\\-\\s.]+")).filter { it.isNotEmpty() }
            if (parts.isEmpty()) return input

            return buildString {
                append(parts[0].replaceFirstChar { it.lowercase() })
                for (i in 1 until parts.size) {
                    append(parts[i].replaceFirstChar { it.uppercase() })
                }
            }
        }

        /**
         * 将字符串转换为 PascalCase。
         */
        internal fun toPascalCase(input: String): String {
            if (input.isBlank()) return input

            val parts = input.split(Regex("[_\\-\\s.]+")).filter { it.isNotEmpty() }
            if (parts.isEmpty()) return input

            return parts.joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
        }
    }
}

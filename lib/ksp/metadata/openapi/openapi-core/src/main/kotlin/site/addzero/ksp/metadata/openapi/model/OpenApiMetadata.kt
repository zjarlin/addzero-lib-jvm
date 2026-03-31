package site.addzero.ksp.metadata.openapi.model

enum class OpenApiFramework {
    SPRING,
    KTOR,
}

enum class OpenApiRouteKind {
    HTTP,
    WEBSOCKET,
    SSE,
    UNKNOWN,
}

enum class OpenApiHttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS,
    TRACE,
}

enum class OpenApiParameterLocation {
    PATH,
    QUERY,
    HEADER,
    COOKIE,
    BODY,
    PART,
    CONTEXT,
    UNKNOWN,
}

enum class OpenApiOwnerKind {
    FILE,
    CONTROLLER,
    ROUTE_FUNCTION,
    ROUTING_BLOCK,
}

data class OpenApiTypeMeta(
    val qualifiedName: String?,
    val displayName: String,
    val nonNullDisplayName: String,
    val nullable: Boolean,
    val collectionLike: Boolean = false,
    val mapLike: Boolean = false,
    val arguments: List<OpenApiTypeMeta> = emptyList(),
) {
    companion object {
        val Star = OpenApiTypeMeta(
            qualifiedName = null,
            displayName = "*",
            nonNullDisplayName = "*",
            nullable = false,
        )
    }
}

data class OpenApiParameterMeta(
    val index: Int,
    val sourceName: String,
    val externalName: String,
    val location: OpenApiParameterLocation,
    val type: OpenApiTypeMeta,
    val required: Boolean,
    val description: String? = null,
)

data class OpenApiRequestBodyMeta(
    val type: OpenApiTypeMeta? = null,
    val required: Boolean = true,
    val contentTypes: List<String> = emptyList(),
    val parts: List<OpenApiParameterMeta> = emptyList(),
    val description: String? = null,
)

data class OpenApiResponseBodyMeta(
    val type: OpenApiTypeMeta?,
    val contentTypes: List<String> = emptyList(),
    val description: String? = null,
)

data class OpenApiOwnerMeta(
    val kind: OpenApiOwnerKind,
    val packageName: String,
    val simpleName: String,
    val qualifiedName: String?,
    val sourceFile: String? = null,
)

data class OpenApiEndpointMeta(
    val framework: OpenApiFramework,
    val owner: OpenApiOwnerMeta,
    val routeKind: OpenApiRouteKind = OpenApiRouteKind.HTTP,
    val httpMethod: OpenApiHttpMethod? = null,
    val path: String,
    val functionName: String? = null,
    val operationId: String,
    val summary: String? = null,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val consumes: List<String> = emptyList(),
    val produces: List<String> = emptyList(),
    val parameters: List<OpenApiParameterMeta> = emptyList(),
    val requestBody: OpenApiRequestBodyMeta? = null,
    val responseBody: OpenApiResponseBodyMeta? = null,
    val isSuspend: Boolean = false,
    val sourceLine: Int? = null,
)

data class OpenApiExtractionWarning(
    val framework: OpenApiFramework,
    val message: String,
    val sourceFile: String? = null,
    val ownerQualifiedName: String? = null,
    val line: Int? = null,
)

data class OpenApiMetadataBundle(
    val framework: OpenApiFramework,
    val endpoints: List<OpenApiEndpointMeta>,
    val warnings: List<OpenApiExtractionWarning> = emptyList(),
)

package site.addzero.springktor.processor

enum class SpringHttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
}

enum class ParameterBindingKind {
    APPLICATION_CALL,
    APPLICATION,
    ROUTING_CONTEXT,
    APPLICATION_REQUEST,
    APPLICATION_RESPONSE,
    PATH_VARIABLE,
    REQUEST_PARAM,
    REQUEST_BODY,
    REQUEST_HEADER,
    REQUEST_PART_VALUE,
    MULTIPART_FILE,
    MULTIPART_FILE_LIST,
}

data class ParameterMeta(
    val index: Int,
    val sourceName: String,
    val externalName: String,
    val typeName: String,
    val nonNullTypeName: String,
    val bindingKind: ParameterBindingKind,
    val nullable: Boolean,
)

data class TopLevelRouteMeta(
    val packageName: String,
    val fileName: String,
    val functionName: String,
    val functionQualifiedName: String,
    val httpMethod: SpringHttpMethod,
    val path: String,
    val parameters: List<ParameterMeta>,
    val returnTypeName: String?,
)

data class ControllerRouteMeta(
    val controllerPackageName: String,
    val controllerSimpleName: String,
    val controllerQualifiedName: String,
    val functionName: String,
    val httpMethod: SpringHttpMethod,
    val path: String,
    val parameters: List<ParameterMeta>,
    val returnTypeName: String?,
)

data class SpringKtorModel(
    val topLevelRoutes: Set<TopLevelRouteMeta>,
    val controllerRoutes: Set<ControllerRouteMeta>,
)

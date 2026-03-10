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

enum class BeanClassKind {
    COMPONENT,
    CONFIGURATION,
    CONTROLLER,
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
    val returnsUnit: Boolean,
    val nullableReturn: Boolean,
    val isSuspend: Boolean,
    val sourceFilePath: String,
)

data class ControllerRouteMeta(
    val controllerPackageName: String,
    val controllerSimpleName: String,
    val controllerQualifiedName: String,
    val functionName: String,
    val httpMethod: SpringHttpMethod,
    val path: String,
    val parameters: List<ParameterMeta>,
    val returnsUnit: Boolean,
    val nullableReturn: Boolean,
    val isSuspend: Boolean,
    val sourceFilePath: String,
)

data class BeanClassMeta(
    val kind: BeanClassKind,
    val qualifiedName: String,
    val simpleName: String,
    val packageName: String,
    val objectDeclaration: Boolean,
    val dependencyCount: Int,
    val sourceFilePath: String,
)

data class BeanFactoryMeta(
    val configurationQualifiedName: String,
    val configurationSimpleName: String,
    val methodName: String,
    val dependencyCount: Int,
    val sourceFilePath: String,
)

data class SpringKtorModel(
    val topLevelRoutes: Set<TopLevelRouteMeta>,
    val controllerRoutes: Set<ControllerRouteMeta>,
    val beanClasses: Set<BeanClassMeta>,
    val beanFactories: Set<BeanFactoryMeta>,
)

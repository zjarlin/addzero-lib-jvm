package site.addzero.processor.feign

data class ControllerMeta(
    val className: String,
    val packageName: String,
    val basePath: String,
    val methods: List<MethodMeta>,
    val description: String = ""
)

data class MethodMeta(
    val name: String,
    val httpMethod: HttpMethod,
    val path: String,
    val returnType: String,
    val parameters: List<ParamMeta>,
    val description: String = ""
)

data class ParamMeta(
    val name: String,
    val type: String,
    val annotation: ParamAnnotation
)

enum class HttpMethod { GET, POST, PUT, DELETE, PATCH }

enum class ParamAnnotation { 
    PATH_VARIABLE, 
    REQUEST_PARAM, 
    REQUEST_BODY, 
    REQUEST_HEADER, 
    NONE 
}

package site.addzero.kcloud.codegen.ir

data class OpenApiIR(
    val interfaces: List<ApiInterfaceIR>,
    val models: List<ModelIR>
)

data class ApiInterfaceIR(
    val name: String,           // 接口名，基于 tag 或文件名
    val operations: List<OperationIR>
)

data class OperationIR(
    val functionName: String,   // camelCase 函数名
    val httpMethod: HttpMethod, // GET, POST, PUT, DELETE
    val path: String,           // 如 "api/favorites/{trackId}"
    val summary: String?,       // KDoc 注释
    val parameters: List<ParameterIR>,
    val requestBody: TypeRef?,  // @Body 参数类型
    val responseType: TypeRef   // 返回类型
)

enum class HttpMethod { GET, POST, PUT, DELETE, PATCH }

data class ParameterIR(
    val name: String,
    val location: ParameterLocation, // PATH, QUERY, HEADER
    val type: TypeRef,
    val required: Boolean
)

enum class ParameterLocation { PATH, QUERY, HEADER }

data class ModelIR(
    val name: String,           // PascalCase 类名
    val properties: List<PropertyIR>,
    val description: String?
)

data class PropertyIR(
    val name: String,           // camelCase 属性名
    val type: TypeRef,
    val required: Boolean,
    val defaultValue: String?,  // Kotlin 字面量表示
    val description: String?
)

sealed class TypeRef {
    data class Primitive(val kotlinType: String) : TypeRef()       // "String", "Int", "Long", "Double", "Float", "Boolean"
    data class ListType(val itemType: TypeRef) : TypeRef()         // List<T>
    data class Reference(val modelName: String) : TypeRef()        // 引用已定义的 model
    data object JsonElement : TypeRef()                            // 回退类型
    data object Unit : TypeRef()                                   // 无返回体
}

sealed class ParseResult {
    data class Success(val ir: OpenApiIR) : ParseResult()
    data class Failure(val errors: List<String>) : ParseResult()
}

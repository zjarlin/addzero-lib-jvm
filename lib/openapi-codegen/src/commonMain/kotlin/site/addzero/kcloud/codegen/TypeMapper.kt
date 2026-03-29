package site.addzero.kcloud.codegen

import io.swagger.v3.oas.models.media.Schema
import site.addzero.kcloud.codegen.ir.TypeRef

/**
 * 将 OpenAPI Schema 类型映射为 Kotlin TypeRef。
 */
object TypeMapper {

    fun map(schema: Schema<*>, schemaName: String? = null): TypeRef {
        // 1. 处理 $ref 引用
        schema.`$ref`?.let { ref ->
            val modelName = ref.substringAfterLast("/")
            return TypeRef.Reference(modelName)
        }

        // 2. 根据 type + format 映射
        return when (schema.type) {
            "string" -> TypeRef.Primitive("String")
            "integer" -> when (schema.format) {
                "int64" -> TypeRef.Primitive("Long")
                else -> TypeRef.Primitive("Int") // int32 或无 format 默认 Int
            }
            "number" -> when (schema.format) {
                "float" -> TypeRef.Primitive("Float")
                else -> TypeRef.Primitive("Double") // double 或无 format 默认 Double
            }
            "boolean" -> TypeRef.Primitive("Boolean")
            "array" -> {
                val itemsSchema = schema.items
                val itemType = if (itemsSchema != null) map(itemsSchema) else TypeRef.JsonElement
                TypeRef.ListType(itemType)
            }
            // 3. 未知/缺失类型回退
            else -> TypeRef.JsonElement
        }
    }
}

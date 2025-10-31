package site.addzero.core.ext

import site.addzero.core.network.json.json
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * 将JSON字符串解析为指定类型
 */
inline fun <reified T> String.parseObjectByKtx(): T {
    return json.decodeFromString(this)
}

/**
 * 将对象转换为JSON字符串
 */
inline fun <reified T> T.toJsonByKtx(): String {
    return json.encodeToString(this)
}

/**
 * 将对象转换为另一个对象
 */
inline fun <reified T> Any.convertToByKtx(): T {
    val toJsonByKtx = this.toJsonByKtx()
    return toJsonByKtx.parseObjectByKtx<T>()
}

// 非内联版本的 Bean 转 Map
@OptIn(InternalSerializationApi::class)
fun <T : Any> T.bean2map(clazz: KClass<T>): Map<String, Any?> {
    val jsonString = json.encodeToString(clazz.serializer(), this)
    return json.parseToJsonElement(jsonString).jsonObject.bean2map()
}

// Bean 转 Map
inline fun <reified T : Any> T.bean2map(): Map<String, Any?> {
    val jsonString = json.encodeToString(this)
    val jsonObject = json.parseToJsonElement(jsonString).jsonObject
    return jsonObj2Map(jsonObject)
}

// 新增：使用 KClass 的 map2bean 方法（非内联）
@OptIn(InternalSerializationApi::class)
fun <T : Any> Map<String, Any?>.map2bean(clazz: KClass<T>): T {
    val jsonElement = json.encodeToString(this)
    return json.decodeFromString(clazz.serializer(), jsonElement)
}

// JsonObject 转 Map
// 添加 JsonElement.toAny 扩展函数用于递归转基本类型
fun JsonElement.toAny(): Any? = when (this) {
    is JsonPrimitive -> this.contentOrNull ?: this.booleanOrNull ?: this.intOrNull ?: this.longOrNull
    ?: this.doubleOrNull

    is JsonObject -> jsonObj2Map(this)
    is JsonArray -> this.map { it.toAny() }
    else -> null
}

// 修复原报错行
fun jsonObj2Map(jsonObject: JsonObject): Map<String, Any?> {
    return jsonObject.mapValues { (_, element) ->
        when (element) {
            is JsonObject -> element.toMap() // 对象类型
            is JsonArray -> element.map { it.toAny() } // 数组类型
            is JsonPrimitive -> element.content // 基本类型
            else -> throw IllegalArgumentException("Unsupported JSON element type")
        }
    }
}

inline fun <reified T> Map<String, Any?>.map2bean(): T {
    val convertToByKtx = this.convertToByKtx<T>()
    return convertToByKtx
}


@OptIn(InternalSerializationApi::class)
fun <T : Any> String.parseListByKtxByKClass(kttype: KClass<T>): List<T> {
    val itemSerializer = kttype.serializer()
    val listSerializer = ListSerializer(itemSerializer)
    val decodeFromString = json.decodeFromString(listSerializer, this)
    return decodeFromString
}



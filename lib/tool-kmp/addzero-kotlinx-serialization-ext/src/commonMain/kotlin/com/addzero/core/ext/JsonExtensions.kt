package com.addzero.core.ext

import com.addzero.core.network.json.json
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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


// Bean 转 Map
inline fun <reified T> T.toMap(): Map<String, Any?> {
    val jsonString = json.encodeToString(this)
    return json.parseToJsonElement(jsonString).jsonObject.toMap()
}

// JsonObject 转 Map
 fun JsonObject.toMap(): Map<String, Any?> {
    return this.mapValues { (_, value) ->
        when (value) {
            is JsonObject -> value.toMap()
            else -> value.jsonPrimitive.contentOrNull
        }
    }
}

inline fun <reified T> Map<String, Any?>.toBean(): T {
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



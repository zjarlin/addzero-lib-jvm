package site.addzero.web.infra.jackson

import cn.hutool.extra.spring.SpringUtil
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import kotlin.reflect.KClass

val objectMapper: ObjectMapper by lazy {
    runCatching { SpringUtil.getBean(ObjectMapper::class.java) }
        .getOrElse { createAddzeroObjectMapper() }
}

inline fun <reified T> JsonNode.getList(): List<T> =
    objectMapper.convertValue(this, object : TypeReference<List<T>>() {})

inline fun <reified T> JsonNode.getMutableList(): MutableList<T> =
    objectMapper.convertValue(this, object : TypeReference<MutableList<T>>() {})

inline fun <reified T> String.parseObject(): T =
    objectMapper.readValue(this, object : TypeReference<T>() {})

fun String.parseObject(): ObjectNode =
    objectMapper.readTree(this) as ObjectNode

fun Any.toJson(): String =
    objectMapper.writeValueAsString(this)

fun <K, V> Map<K, V>.toJson(): String =
    objectMapper.writeValueAsString(this)

fun String.toJsonNode(): JsonNode =
    objectMapper.readTree(this)

inline fun <reified K, reified V> String.toMap(): Map<K, V> =
    objectMapper.readValue(this, object : TypeReference<Map<K, V>>() {})

inline fun <reified T> String.toList(): List<T> =
    objectMapper.readValue(this, object : TypeReference<List<T>>() {})

inline fun <reified K : Any, reified V : Any> String.toMapFromArray(
    fieldName: String,
    keyField: String,
    valueField: String,
): Map<K, V> {
    val node: JsonNode = objectMapper.readTree(this)
    return node[fieldName].associate { item ->
        val key = objectMapper.convertValue(item[keyField], K::class.java)
        val value = objectMapper.convertValue(item[valueField], V::class.java)
        key to value
    }
}

inline fun <reified T> Any.convertTo(): T {
    val toJson = this.toJson()
    return toJson.parseObject<T>()
}

fun <T : Any> Any.convertToList(kclass: KClass<T>): List<T> {
    val toJson = this.toJson()
    val javaType = objectMapper.typeFactory.constructCollectionType(List::class.java, kclass.java)
    return objectMapper.readValue(toJson, javaType)
}

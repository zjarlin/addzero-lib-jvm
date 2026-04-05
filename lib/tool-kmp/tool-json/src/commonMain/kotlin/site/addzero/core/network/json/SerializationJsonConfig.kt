package site.addzero.core.network.json

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

private val defaultJsonSerializersModule = SerializersModule {
    contextual(Any::class, AnySerializer)
    contextual(Instant::class, Instant.serializer())
    contextual(LocalDate::class, LocalDate.serializer())
    contextual(LocalDateTime::class, LocalDateTime.serializer())
    contextual(LocalTime::class, LocalTime.serializer())
}

val json = Json {
    encodeDefaults = true
    //显示null
//    explicitNulls = true
    ignoreUnknownKeys = true
    isLenient = true
//    prettyPrint = true
    useAlternativeNames = false
    // 允许将值强制转换为目标类型
    coerceInputValues = true
    //注册Any序列化器
    serializersModule = defaultJsonSerializersModule
}

val prettyJson = Json(json) {
    prettyPrint = true
}

val strictJson = Json(json) {
    ignoreUnknownKeys = false
}

val omitNullJson = Json(json) {
    explicitNulls = false
}

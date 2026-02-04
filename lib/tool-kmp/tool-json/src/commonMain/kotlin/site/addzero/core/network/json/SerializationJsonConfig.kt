package site.addzero.core.network.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

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
    serializersModule = SerializersModule {
        contextual(Any::class, AnySerializer)
    }
}

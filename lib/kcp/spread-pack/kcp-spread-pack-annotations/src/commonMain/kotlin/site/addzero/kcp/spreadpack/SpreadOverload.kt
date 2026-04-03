package site.addzero.kcp.spreadpack

import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
annotation class SpreadOverload(
    val of: SpreadOverloadsOf,
    val parameterTypes: Array<KClass<*>> = [],
)

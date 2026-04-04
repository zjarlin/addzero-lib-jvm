package site.addzero.kcp.spreadpack

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class SpreadPackCarrierOf(
    val value: String = "",
    val parameterTypes: Array<KClass<*>> = [],
    val exclude: Array<String> = [],
    val selector: SpreadPackSelector = SpreadPackSelector.PROPS,
)

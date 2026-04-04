package site.addzero.kcp.spreadpack

import kotlin.reflect.KClass

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class SpreadPackOf(
    val value: String = "",
    val parameterTypes: Array<KClass<*>> = [],
    val exclude: Array<String> = [],
    val selector: SpreadPackSelector = SpreadPackSelector.PROPS,
    val generatedClassName: String = "",
)

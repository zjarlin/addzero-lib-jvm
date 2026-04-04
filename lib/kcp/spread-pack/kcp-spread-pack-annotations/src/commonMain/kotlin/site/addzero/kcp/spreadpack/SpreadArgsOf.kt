package site.addzero.kcp.spreadpack

import kotlin.reflect.KClass

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class SpreadArgsOf(
    val value: String = "",
    val functionFqName: String = "",
    val parameterTypes: Array<KClass<*>> = [],
    val exclude: Array<String> = [],
    val selector: SpreadPackSelector = SpreadPackSelector.PROPS,
    val overload: SpreadOverload = SpreadOverload(
        of = SpreadOverloadsOf(""),
    ),
)

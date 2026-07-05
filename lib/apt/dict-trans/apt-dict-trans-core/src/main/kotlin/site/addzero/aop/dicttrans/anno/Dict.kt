package site.addzero.aop.dicttrans.anno

import kotlin.reflect.KClass

/**
 * @author addzero
 * @since 2022/11/10 14:05
 */
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.FIELD,
)
@Retention(
    AnnotationRetention.RUNTIME
)
@Repeatable
annotation class Dict(
    val value: String = "",
    val dicCode: String = "",
    //如果不是系统内置字典会用到下面的参数
    val tab: String = "",
    val codeColumn: String = "",
    val nameColumn: String = "",
    val whereCondition: String = "",
    val spelExp: String = "",
    val spelValueType: KClass<*> = String::class,
    val serializationAlias: String = "",
    val serializationChinese: String = "",
    val ignoreVo: Boolean = false,
)

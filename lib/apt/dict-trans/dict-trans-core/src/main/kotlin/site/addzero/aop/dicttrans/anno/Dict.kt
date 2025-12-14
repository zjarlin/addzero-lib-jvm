package site.addzero.aop.dicttrans.anno

/**
 * @author addzero
 * @since 2022/11/10 14:05
 */
@Target(
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
    val serializationAlias: String = "",
)

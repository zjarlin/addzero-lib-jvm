package site.addzero.mybatis.auto_wrapper

/**
 * @author xiaokedamowang
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Wheres(
    vararg val value: Where,
    val group: String = "",
    val outerJoin: Boolean = false,
    val innerJoin: Boolean = false
)

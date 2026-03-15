package site.addzero.springktor.runtime

@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
annotation class RequestMapping(
    val value: String = "",
    val path: String = "",
)

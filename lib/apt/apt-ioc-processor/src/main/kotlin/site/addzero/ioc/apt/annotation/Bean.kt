package site.addzero.ioc.annotation

/**
 * 标记函数或类为需要自动初始化的 Bean
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
annotation class Bean
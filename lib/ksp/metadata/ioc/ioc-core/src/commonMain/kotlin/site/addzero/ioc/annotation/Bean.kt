package site.addzero.ioc.annotation

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

/**
 * Marks a function or class as a managed bean in the IoC container.
 *
 * - On a class: registers it as a bean (with optional interface implementation tracking)
 * - On a function: registers it for aggregated execution
 * - On an object: registers the singleton instance
 *
 * @param name optional bean name, defaults to simple class/function name
 * @param order execution order, smaller values run first
 * @param tags optional tags for grouping, e.g. `@Bean(tags = ["page", "admin"])`
 */
@Target(FUNCTION, CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Bean(
    val name: String = "",
    val order: Int = 0,
    val tags: Array<String> = []
)

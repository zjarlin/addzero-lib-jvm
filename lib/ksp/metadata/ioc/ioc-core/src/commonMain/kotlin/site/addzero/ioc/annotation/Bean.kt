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
 * @param enable whether this bean should participate in registration/execution
 * @param order execution order, smaller values run first when there is no dependsOn edge
 * @param tags optional tags for grouping, e.g. `@Bean(tags = ["page", "admin"])`
 * @param dependsOn optional bean names / simple names / qualified names that must run first
 */
@Target(FUNCTION, CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Bean(
    val name: String = "",
    val enable: Boolean = true,
    val order: Int = Int.MAX_VALUE,
    val tags: Array<String> = [],
    val dependsOn: Array<String> = []
)

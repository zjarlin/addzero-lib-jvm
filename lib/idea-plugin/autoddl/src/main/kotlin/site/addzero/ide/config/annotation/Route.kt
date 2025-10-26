package site.addzero.ide.config.annotation

import kotlin.reflect.KClass

/**
 * 路由注解，用于定义配置项在设置面板中的路径
 *
 * @param path 配置项在设置面板中的路径，每个字符串代表树形结构中的一级
 * @param configClass 配置数据类
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Route(
    vararg val path: String,
    val configClass: KClass<*>
)
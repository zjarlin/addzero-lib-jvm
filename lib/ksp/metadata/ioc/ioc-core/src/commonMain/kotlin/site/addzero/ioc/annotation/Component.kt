package site.addzero.ioc.annotation

import kotlin.annotation.AnnotationTarget.CLASS

/**
 * 标记类为可注册的组件，支持策略模式使用
 */
@Target(CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Component(
    /**
     * 组件名称，如果不指定则使用类名首字母小写
     */
    val value: String = ""
)
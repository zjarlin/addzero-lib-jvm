package com.addzero.dsl

/**
 * 用于生成DSL风格构建器的注解
 * 在实体类上添加此注解，将自动生成基于属性委托的DSL风格构建器函数
 *
 * @param value 自定义DSL函数名称，默认为空，表示使用类名转小驼峰
 * @param removePrefix 移除类名前缀，默认为空
 * @param removeSuffix 移除类名后缀，默认为空
 * @param genCollectionDslBuilder 是否生成集合DSL构建器，默认为false
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Dsl(
    val value: String = "",
    val removePrefix: String = "",

    val removeSuffix: String = "",
    val genCollectionDslBuilder: Boolean = false
)

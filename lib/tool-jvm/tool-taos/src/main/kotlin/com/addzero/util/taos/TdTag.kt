package com.addzero.util.taos

/**
 * 用于标识TDengine超级表的标签字段
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class TdTag(
    /**
     * 标签的名称，默认使用字段名
     */
    val name: String = "",
    
    /**
     * 标签的类型，默认为BINARY(255)
     */
    val type: String = "BINARY(255)"
)
package site.addzero.autoinit.annotation

import kotlin.annotation.AnnotationTarget.FUNCTION

// 标记需要自动初始化的函数
@Target(FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AutoInit

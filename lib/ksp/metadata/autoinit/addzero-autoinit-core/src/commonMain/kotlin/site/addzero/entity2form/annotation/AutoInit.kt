package com.example.autoinit

import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.RetentionPolicy.RUNTIME

// 标记需要自动初始化的函数
@Target(FUNCTION)
@Retention(RUNTIME)
annotation class AutoInit

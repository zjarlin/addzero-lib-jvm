package com.addzero.aop.dicttrans.inter

import kotlin.reflect.KClass

interface TPredicate {
    fun tBlackList(): List<Class<out Any>>
}

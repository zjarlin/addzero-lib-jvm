package site.addzero.mybatis.auto_wrapper

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper

internal interface JoinAndNested<T, R> {
    val innerJoin: Boolean
        get() = false

    val condition
        get() = false
    fun process(clazz: Class<T>, wrapper: AbstractWrapper<T, R, *>)
}

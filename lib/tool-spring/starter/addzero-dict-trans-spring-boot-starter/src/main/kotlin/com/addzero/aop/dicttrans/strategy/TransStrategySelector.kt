package com.addzero.aop.dicttrans.strategy

import com.addzero.aop.dicttrans.inter.TransStrategy
import org.springframework.stereotype.Component


/**
 * @author zjarlin
 * @since 2023/11/8 10:31
 */
@Component
class TransStrategySelector (
private val transStrategys: List<TransStrategy<*>>
){
    fun getStrategy(t: Any): TransStrategy<Any>? {
        val firstOrNull = transStrategys.firstOrNull { it.support(t) }
        val strategy = firstOrNull
        return strategy as TransStrategy<Any>?
    }


}

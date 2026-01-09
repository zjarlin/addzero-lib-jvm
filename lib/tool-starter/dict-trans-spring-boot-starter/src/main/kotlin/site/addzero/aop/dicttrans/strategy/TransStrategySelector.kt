package site.addzero.aop.dicttrans.strategy

import site.addzero.aop.dicttrans.inter.TransStrategy

class TransStrategySelector(
    private val transStrategies: List<TransStrategy<*>>,
) {
    @Suppress("UNCHECKED_CAST")
    fun getStrategy(target: Any?): TransStrategy<Any>? {
        target ?: return null
        return transStrategies.firstOrNull { it.support(target) } as TransStrategy<Any>?
    }
}

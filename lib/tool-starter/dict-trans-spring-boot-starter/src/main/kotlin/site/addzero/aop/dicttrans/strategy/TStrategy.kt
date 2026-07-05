package site.addzero.aop.dicttrans.strategy

import site.addzero.aop.dicttrans.inter.TransStrategy
import site.addzero.util.RefUtil
import org.springframework.stereotype.Component
import site.addzero.aop.dicttrans.inter.TPredicate
import site.addzero.aop.dicttrans.util_internal.JimmerDictSupport

/**
 * @author zjarlin
 * @since 2023/11/8 11:15
 */

@Component
class TStrategy(private val tPredicate: TPredicate) : TransStrategy<Any?> {

    public override fun trans(o: Any?): Any? {
        val list = mutableListOf(o)
        val collectionStrategy = CollectionStrategy()
        val trans = collectionStrategy.trans(list)
        if (trans.isEmpty()) {
            return null
        }
        return trans.iterator().next()
    }

    override fun support(t: Any): Boolean {
        if (t is Collection<*> || t is Map<*, *> || t is String) {
            return false
        }
        if (JimmerDictSupport.isJimmerObject(t) || JimmerDictSupport.containsJimmerValue(t)) {
            return true
        }
        return RefUtil.isT(t, *tPredicate.tBlackList().toTypedArray<Class<out Any>>())
    }
}

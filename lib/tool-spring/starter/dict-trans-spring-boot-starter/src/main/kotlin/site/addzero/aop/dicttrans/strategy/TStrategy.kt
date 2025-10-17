package site.addzero.aop.dicttrans.strategy

import cn.hutool.core.collection.CollUtil
import site.addzero.aop.dicttrans.inter.TransStrategy
import site.addzero.util.RefUtil
import org.springframework.stereotype.Component
import site.addzero.aop.dicttrans.inter.TPredicate

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
        if (CollUtil.isEmpty(trans)) {
            return null
        }
        return trans.iterator().next()
    }

    override fun support(t: Any): Boolean {
        return RefUtil.isT(t, *tPredicate.tBlackList().toTypedArray<Class<out Any>>())
    }
}

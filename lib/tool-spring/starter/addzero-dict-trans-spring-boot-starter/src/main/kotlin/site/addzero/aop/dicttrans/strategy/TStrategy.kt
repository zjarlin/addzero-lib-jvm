package site.addzero.aop.dicttrans.strategy

import cn.hutool.core.collection.CollUtil
import site.addzero.aop.dicttrans.inter.TransStrategy
import site.addzero.aop.dicttrans.util_internal.RefUtil
import org.springframework.stereotype.Component

/**
 * @author zjarlin
 * @since 2023/11/8 11:15
 */

@Component
class TStrategy : TransStrategy<Any?> {

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
        return RefUtil.isT(t)
    }
}

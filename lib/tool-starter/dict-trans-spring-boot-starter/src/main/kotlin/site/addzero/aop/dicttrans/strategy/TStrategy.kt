package site.addzero.aop.dicttrans.strategy

import cn.hutool.core.collection.CollUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import site.addzero.aop.dicttrans.inter.TransStrategy
import site.addzero.util.RefUtil
import org.springframework.stereotype.Component
import site.addzero.aop.dicttrans.inter.TPredicate

/**
 * Enhanced T strategy with memory management
 * 
 * @author zjarlin
 * @since 2023/11/8 11:15
 */
@Component
class TStrategy @Autowired constructor(
    private val tPredicate: TPredicate,
    private val collectionStrategy: CollectionStrategy
) : TransStrategy<Any?> {
    
    private val logger = LoggerFactory.getLogger(TStrategy::class.java)

    public override fun trans(o: Any?): Any? {
        o ?: return null
        
        logger.debug("Processing single object of type: {}", o.javaClass.name)
        
        val list = mutableListOf(o)
        val trans = try {
            collectionStrategy.trans(list)
        } catch (e: Exception) {
            logger.error("Failed to process object of type: {}", o.javaClass.name, e)
            return o
        }
        
        if (CollUtil.isEmpty(trans)) {
            logger.debug("No result from collection strategy for object: {}", o.javaClass.name)
            return null
        }
        
        return trans.iterator().next()
    }

    override fun support(t: Any): Boolean {
        return RefUtil.isT(t, *tPredicate.tBlackList().toTypedArray<Class<out Any>>())
    }
}

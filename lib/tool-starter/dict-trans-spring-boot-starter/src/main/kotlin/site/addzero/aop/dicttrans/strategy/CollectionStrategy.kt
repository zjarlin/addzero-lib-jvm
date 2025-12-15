package site.addzero.aop.dicttrans.strategy

import org.springframework.stereotype.Component
import site.addzero.aop.dicttrans.inter.TransStrategy
import site.addzero.aop.dicttrans.util_internal.OptimizedByteBuddyUtil
import site.addzero.aop.dicttrans.util_internal.TransInternalUtil
import java.util.*

/**
 * @author zjarlin
 * @since 2023/11/8 10:31
 */
@Component
class CollectionStrategy : TransStrategy<Collection<*>> {
    override fun trans(t: Collection<*>): Collection<*> {
        var inVOs = t
        if (inVOs.isEmpty()) {
            return inVOs
        }

        inVOs = inVOs.filter { e -> Objects.nonNull(e) }
        if (inVOs.isEmpty()) {
            return inVOs
        }
        val size = inVOs.size
        if (size > 1000) {
            println("集合数量为$size,超过1000条跳过字典翻译")
        }

        // 使用优化的批量处理工具，自动收集所有对象类型的字段需求并集，每个类型只生成一次字节码
        val collect = OptimizedByteBuddyUtil.genChildObjectsBatch(inVOs.toList()) { obj ->
            TransInternalUtil.getNeedAddFields(obj).toMutableList()
        }

        //翻译过程的全部信息都在这里了 对于单个字典翻译,会按照list中所有dictCode分组TransInfo集合 会调用系统字段批量翻译
        val collect1 = collect.filter {
            it != null
        }.flatMap {
            val process = TransInternalUtil.process(it!!)
            process
        }.groupBy { it.classificationOfTranslation }
        /**  处理内置字典翻译 */
        TransInternalUtil.processBuiltInDictionaryTranslation(collect1)
        /** 处理任意表翻译  */
        TransInternalUtil.processAnyTableTranslation(collect1)
        /** 处理spel表达式  */
//        TransUtil.processingSpelExpressions(collect1)
        return collect
    }

    override fun support(t: Any): Boolean {
        return Collection::class.java.isAssignableFrom(t.javaClass)
    }
}

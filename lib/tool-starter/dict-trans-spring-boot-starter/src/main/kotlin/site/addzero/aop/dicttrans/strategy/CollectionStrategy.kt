package site.addzero.aop.dicttrans.strategy

import org.springframework.stereotype.Component
import site.addzero.aop.dicttrans.inter.TransStrategy
import site.addzero.aop.dicttrans.util_internal.ByteBuddyUtil
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

        //这里字节码工具还有优化空间 不同形状的对象,可能 getNeedAddFields都不一样,其实找到字段(注解上指定的name)的并集 ,调用一次字节码生成最全字段的代理对象即可,这里先采用每个对象都创建新字节码的方式
        val collect = inVOs.map { e ->
            val o = ByteBuddyUtil.genChildObjectRecursion(e, {
                val needAddFields = TransInternalUtil.getNeedAddFields(it)
                needAddFields.toMutableList()
            })
            o
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

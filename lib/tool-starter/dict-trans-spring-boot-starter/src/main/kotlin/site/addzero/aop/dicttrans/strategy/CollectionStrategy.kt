package site.addzero.aop.dicttrans.strategy

import org.springframework.stereotype.Component
import org.babyfish.jimmer.runtime.ImmutableSpi
import site.addzero.aop.dicttrans.inter.TransStrategy
import site.addzero.aop.dicttrans.util_internal.JimmerDictSupport
import site.addzero.aop.dicttrans.util_internal.TransInternalUtil
import site.addzero.tool.bytebuddy.ByteBuddyUtil
import site.addzero.tool.bytebuddy.ByteBuddyUtil.DynamicFieldDefinition
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

        val sourceList = inVOs.toList()
        val ordinaryObjects = sourceList.filterNot(JimmerDictSupport::isJimmerObject)
        val jimmerObjects = sourceList.filterIsInstance<ImmutableSpi>()

        ordinaryObjects.forEach(JimmerDictSupport::translateNestedJimmerValues)

        // 使用优化的批量处理工具，自动收集所有对象类型的字段需求并集，每个类型只生成一次字节码。
        // Jimmer 实体是 immutable 运行时类型，不能安全地用 ByteBuddy 继承和写入字段，单独转为响应 Map。
        val enhancedOrdinaryObjects = if (ordinaryObjects.isEmpty()) {
            emptyList<Any?>()
        } else {
            ByteBuddyUtil.genChildObjectsBatch(ordinaryObjects) { obj ->
                TransInternalUtil.getNeedAddFields(obj).map { need ->
                    DynamicFieldDefinition(need.fieldName, need.type)
                }
            }
        }
        val ordinaryIterator = enhancedOrdinaryObjects.iterator()
        val jimmerIterator = JimmerDictSupport.translateAll(jimmerObjects).iterator()
        val collect = sourceList.map { obj ->
            when (obj) {
                is ImmutableSpi -> jimmerIterator.next()
                else -> ordinaryIterator.next()
            }
        }

        //翻译过程的全部信息都在这里了 对于单个字典翻译,会按照list中所有dictCode分组TransInfo集合 会调用系统字段批量翻译
        val collect1 = enhancedOrdinaryObjects.filter {
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

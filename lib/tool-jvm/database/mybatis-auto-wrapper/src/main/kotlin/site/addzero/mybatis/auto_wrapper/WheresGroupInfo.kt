package site.addzero.mybatis.auto_wrapper

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper

internal class WheresGroupInfo<T, R>(
    var groupName: String?,
    var outerJoin: Boolean,
    override var innerJoin: Boolean
) : Comparable<WheresGroupInfo<*, *>>, JoinAndNested<T, R> {

    var columnGroupInfoMap = mutableMapOf<String, ColumnGroupInfo<T, R>>()

    override fun compareTo(other: WheresGroupInfo<*, *>): Int {
        val compare = this.outerJoin.compareTo(other.outerJoin)
        return if (compare != 0) compare else (this.groupName ?: "").compareTo(other.groupName ?: "")
    }

    override val condition: Boolean
        get() = columnGroupInfoMap.values.any { it.condition }

    @Suppress("UNCHECKED_CAST")
    private fun getBiConsumer(
        join: Boolean,
        wrapper: AbstractWrapper<*, *, *>
    ): (Boolean, (AbstractWrapper<T, R, *>) -> Unit) -> Unit {
        return if (join) {
            { condition: Boolean, consumer: (AbstractWrapper<T, R, *>) -> Unit ->
                (wrapper as AbstractWrapper<T, R, *>).or(condition, consumer)
            }
        } else {
            { condition: Boolean, consumer: (AbstractWrapper<T, R, *>) -> Unit ->
                (wrapper as AbstractWrapper<T, R, *>).and(condition, consumer)
            }
        }
    }

    override fun process(clazz: Class<T>, wrapper: AbstractWrapper<T, R, *>) {
        getBiConsumer(outerJoin, wrapper)(condition) { innerWrapper ->
            columnGroupInfoMap.values.forEach { columnGroupInfo ->
                getBiConsumer(innerJoin, innerWrapper)(
                    columnGroupInfo.condition
                ) { iInnerWrapper ->
                    columnGroupInfo.process(clazz, iInnerWrapper)
                }
            }
        }
    }
}
package site.addzero.mybatis.auto_wrapper

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper

internal class ColumnGroupInfo<T, R> : JoinAndNested<T, R> {

    var columnInfos = mutableListOf<ColumnInfo<T, R>>()

    override val condition: Boolean
        get() {
            return columnInfos.any { it.condition }
        }
    override val innerJoin: Boolean
        get() {
            return columnInfos.any { it.join }
        }


    override fun process(clazz: Class<T>, wrapper: AbstractWrapper<T, R, *>) {
        columnInfos.forEachIndexed { index, columnInfo ->
            if (index > 0 && this.innerJoin) {
                wrapper.or()
            }
            columnInfo.process(clazz, wrapper)
        }
    }
}

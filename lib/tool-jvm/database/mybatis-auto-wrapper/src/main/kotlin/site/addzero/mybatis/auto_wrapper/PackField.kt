package site.addzero.mybatis.auto_wrapper

import java.lang.reflect.Field

internal abstract class PackField protected constructor(protected var field: Field) {
    protected var fieldName: String = field.name

    protected fun <T, R> createListColumnInfo(
        ans: Array<out Where>,
        value: Any?,
        columnProcess: (Class<T>, String) -> R
    ): MutableList<ColumnInfo<T, R>> {
        val toMutableList = ans.filterNot { it.ignore }.map {
            val columnInfo = ColumnInfo(
                symbol = it.value,
                column = it.column.takeIf { it -> it.isNotEmpty() } ?: fieldName,
                value = value,
                join = it.join,
                spelCondition = it.condition,
                field = field,
                columnProcess = columnProcess
            )
            columnInfo
        }.toMutableList()
        return toMutableList
    }

    abstract fun <T, R> getColumnInfoList(obj: Any?, columnProcess: (Class<T>, String) -> R): MutableList<ColumnInfo<T, R>>

    abstract fun getAnnotation(): Where?

    abstract fun getGroupName(): String?

    abstract fun getOuterJoin(): Boolean

    abstract fun getInnerJoin(): Boolean
}

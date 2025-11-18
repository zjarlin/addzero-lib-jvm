package site.addzero.mybatis.auto_wrapper

import site.addzero.util.spring.SpELUtils
import java.lang.reflect.Field

internal abstract class PackField protected constructor(protected var field: Field) {
    protected var fieldName: String = field.name

    protected fun <T, R> createListColumnInfo(
        ans: Array<out Where>,
        value: Any?,
        dto: Any?,
        columnProcess: (Class<T>, String) -> R
    ): MutableList<ColumnInfo<T, R>> {
        val toMutableList = ans.filterNot { shouldIgnore(it, value, dto) }.map {
            val columnInfo = ColumnInfo(
                symbol = it.value,
                column = it.column.takeIf { column -> column.isNotEmpty() } ?: fieldName,
                value = value,
                join = it.join,
                conditionExpression = it.condition.takeIf { columnCondition -> !it.ignore && columnCondition.isNotBlank() },
                field = field,
                columnProcess = columnProcess
            )
            columnInfo
        }.toMutableList()
        return toMutableList
    }

    private fun shouldIgnore(where: Where, value: Any?, dto: Any?): Boolean {
        if (!where.ignore) {
            return false
        }
        if (where.condition.isNotBlank()) {
            val variables = mutableMapOf(
                "value" to value,
                "field" to field,
                "dto" to dto
            )
            return try {
                SpELUtils.evaluateExpression(variables, where.condition, Boolean::class.java) == true
            } catch (e: Exception) {
                false
            }
        }
        return true
    }

    abstract fun <T, R> getColumnInfoList(obj: Any?, columnProcess: (Class<T>, String) -> R): MutableList<ColumnInfo<T, R>>

    abstract fun getAnnotation(): Where?

    abstract fun getGroupName(): String?

    abstract fun getOuterJoin(): Boolean

    abstract fun getInnerJoin(): Boolean
}

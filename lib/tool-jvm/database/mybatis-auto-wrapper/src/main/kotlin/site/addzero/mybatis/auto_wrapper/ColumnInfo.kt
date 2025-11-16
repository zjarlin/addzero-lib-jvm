package site.addzero.mybatis.auto_wrapper

import cn.hutool.core.util.ObjUtil
import cn.hutool.core.util.StrUtil
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import site.addzero.web.infra.spring.SpELUtils
import java.lang.reflect.Array as JArray
import java.lang.reflect.Field

internal class ColumnInfo<T, R>(
    var symbol: String, var column: String, var value: Any?, var join: Boolean, var spelCondition: String?, var field: Field, var columnProcess: (Class<T>, String) -> R
) : JoinAndNested<T, R> {
    var dto: Any? = null

    override val condition = if (spelCondition.isNullOrBlank()) {
        // 默认逻辑：对于 null 和 notNull 操作符，逻辑相反
        when (symbol) {
            "null" -> {
                // null 操作符：只有当值为 null 时才生成 IS NULL 条件
                value == null
            }
            "notNull" -> {
                // notNull 操作符：只有当值不为 null 时才生成 IS NOT NULL 条件
                value != null
            }
            else -> {
                // 其他操作符（=, !=, >, <, like等）：值不为空时才应用
                ObjUtil.isNotEmpty(value)
            }
        }
    } else {
        val variables = HashMap<String, Any?>()
        variables["value"] = value
        variables["field"] = field
        variables["dto"] = dto
        val spelResult = SpELUtils.evaluateExpression(variables, spelCondition!!, Boolean::class.java)
        spelResult == true
    }

    override fun process(clazz: Class<T>, wrapper: AbstractWrapper<T, R, *>) {
        val r = columnProcess(clazz, this.column)
        whereSwitch(wrapper, r, this)
    }

    companion object {
        private fun <T, R, W : AbstractWrapper<T, R, W>> whereSwitch(queryWrapper: AbstractWrapper<T, R, W>, r: R, columnInfo: ColumnInfo<T, R>) {
            when (columnInfo.symbol) {
                "=" -> {
                    queryWrapper.eq(columnInfo.condition, r, columnInfo.value)
                }

                "!=" -> {
                    queryWrapper.ne(columnInfo.condition, r, columnInfo.value)
                }

                "null" -> {
                    queryWrapper.isNull(columnInfo.condition, r)
                }

                "notNull" -> {
                    queryWrapper.isNotNull(columnInfo.condition, r)
                }

                "in" -> {
                    columnInfo.collectionValue()?.let { queryWrapper.`in`(columnInfo.condition, r, it) }
                }

                "notIn" -> {
                    columnInfo.collectionValue()?.let { queryWrapper.notIn(columnInfo.condition, r, it) }
                }

                "findInSet" -> {
                    queryWrapper.findInSet(columnInfo.condition, r, columnInfo)
                }

                "like" -> {
                    queryWrapper.like(columnInfo.condition, r, columnInfo.value)
                }

                "like%" -> {
                    queryWrapper.likeRight(columnInfo.condition, r, columnInfo.value)
                }

                "<" -> {
                    queryWrapper.lt(columnInfo.condition, r, columnInfo.value)
                }

                "<=" -> {
                    queryWrapper.le(columnInfo.condition, r, columnInfo.value)
                }

                ">" -> {
                    queryWrapper.gt(columnInfo.condition, r, columnInfo.value)
                }

                ">=" -> {
                    queryWrapper.ge(columnInfo.condition, r, columnInfo.value)
                }

                else -> {}
            }
        }
        
        private fun <T, R, W : AbstractWrapper<T, R, W>> AbstractWrapper<T, R, W>.findInSet(condition: Boolean, r: R, columnInfo: ColumnInfo<T, R>) {
            if (!condition) {
                return
            }
            val candidates = columnInfo.collectionValue()
                ?.mapNotNull { it?.toString()?.trim()?.takeIf { str -> str.isNotEmpty() } }
                ?.takeIf { it.isNotEmpty() }
                ?: return

            val columnName = when (r) {
                is String -> r
                else -> columnInfo.column
            }
            val format = StrUtil.format("FIND_IN_SET({0}, {}) > 0", columnName)
            this.and { inner ->
                candidates.forEach { candidate ->
                    inner.or { it.apply(format, candidate) }
                }
            }
        }

        private fun ColumnInfo<*, *>.collectionValue(): MutableCollection<*>? {
            val currentValue = value ?: return null
            val collection: MutableCollection<*> = when (currentValue) {
                is MutableCollection<*> -> currentValue
                is Collection<*> -> currentValue.toMutableList()
                is Array<*> -> currentValue.toMutableList()
                is BooleanArray -> currentValue.toList().toMutableList()
                is ByteArray -> currentValue.toList().toMutableList()
                is CharArray -> currentValue.toList().toMutableList()
                is ShortArray -> currentValue.toList().toMutableList()
                is IntArray -> currentValue.toList().toMutableList()
                is LongArray -> currentValue.toList().toMutableList()
                is FloatArray -> currentValue.toList().toMutableList()
                is DoubleArray -> currentValue.toList().toMutableList()
                is String -> currentValue.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                else -> {
                    if (currentValue.javaClass.isArray) {
                        val length = JArray.getLength(currentValue)
                        MutableList(length) { index -> JArray.get(currentValue, index) }
                    } else {
                        return null
                    }
                }
            }
            return collection.takeIf { it.isNotEmpty() }
        }
    }
}

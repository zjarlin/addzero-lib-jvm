package site.addzero.mybatis.auto_wrapper

import cn.hutool.core.util.ObjUtil
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import site.addzero.web.infra.spring.SpELUtils
import java.lang.reflect.Field

internal class ColumnInfo<T, R>(
    var symbol: String, var column: String, var value: Any?, var join: Boolean, var spelCondition: String?, var field: Field, var columnProcess: (Class<T>, String) -> R
) : JoinAndNested<T, R> {
    var dto: Any? = null

    override val condition = if (spelCondition.isNullOrBlank()) {
        // 默认逻辑：对于 null 和 notNull 操作符，逻辑相反
        if ("null" == symbol) {
            // null 操作符：当值为 null 时才生成 IS NULL 条件
            value == null
        }
        val notEmpty = ObjUtil.isNotEmpty(value)
        notEmpty
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
                    queryWrapper.`in`(columnInfo.condition, r, columnInfo.value as MutableCollection<*>)
                }

                "notIn" -> {
                    queryWrapper.notIn(columnInfo.condition, r, columnInfo.value as MutableCollection<*>)
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

    }
}

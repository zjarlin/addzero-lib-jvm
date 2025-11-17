package site.addzero.mybatis.auto_wrapper

import cn.hutool.core.util.ReflectUtil
import com.baomidou.mybatisplus.annotation.TableField
import java.lang.reflect.Field

/**
 * Fallback packer for fields without @Where annotations.
 */
internal class DefaultPackField(field: Field) : PackField(field) {
    private val tableField: TableField? = field.getAnnotation(TableField::class.java)

    override fun <T, R> getColumnInfoList(
        obj: Any?,
        columnProcess: (Class<T>, String) -> R
    ): MutableList<ColumnInfo<T, R>> {
        if (tableField?.exist == false) {
            return mutableListOf()
        }
        val value = ReflectUtil.getFieldValue(obj, field)
        val columnName = tableField?.value?.takeIf { it.isNotBlank() } ?: fieldName
        val columnInfo = ColumnInfo(
            symbol = "=",
            column = columnName,
            value = value,
            join = false,
            conditionExpression = null,
            field = field,
            columnProcess = columnProcess
        )
        columnInfo.dto = obj
        return mutableListOf(columnInfo)
    }

    override fun getAnnotation(): Where? = null

    override fun getGroupName(): String? = fieldName

    override fun getOuterJoin(): Boolean = false

    override fun getInnerJoin(): Boolean = false
}

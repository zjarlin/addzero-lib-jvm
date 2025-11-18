package site.addzero.mybatis.auto_wrapper

import cn.hutool.core.util.ReflectUtil.getFieldValue
import java.lang.reflect.Field

internal class WherePackField(field: Field) : PackField(field) {
    private val an: Where? = field.getAnnotation(Where::class.java)


    override fun <T, R> getColumnInfoList(obj: Any?, columnProcess: (Class<T>, String) -> R): MutableList<ColumnInfo<T, R>> {
        val annotation = field.getAnnotation(Where::class.java)
        val columnInfoList = createListColumnInfo(arrayOf(annotation), getFieldValue(obj, field), obj, columnProcess)
        columnInfoList.forEach { it.dto = obj }
        return columnInfoList
    }

    override fun getAnnotation(): Where? {
        return an
    }

    override fun getGroupName(): String? {
        return fieldName
    }

    override fun getOuterJoin(): Boolean {
        return false
    }

    override fun getInnerJoin(): Boolean {
        return false
    }
}

package site.addzero.mybatis.auto_wrapper

import cn.hutool.core.util.ReflectUtil
import java.lang.reflect.Field

internal class WheresPackField(field: Field) : PackField(field) {
    private val an = field.getAnnotation(Wheres::class.java)


    override fun <T, R> getColumnInfoList(obj: Any?, columnProcess: (Class<T>, String) -> R): MutableList<ColumnInfo<T, R>> {
        val ans = this.an?.value ?: emptyArray()
        val value = ReflectUtil.getFieldValue(obj, field)
        val createListColumnInfo = createListColumnInfo(ans, value, columnProcess)
        return createListColumnInfo
    }

    override fun getAnnotation(): Where? {
        return an?.value?.firstOrNull()
    }

    override fun getGroupName(): String? {
        return an?.group
    }

    override fun getOuterJoin(): Boolean {
        return an?.outerJoin ?: false
    }

    override fun getInnerJoin(): Boolean {
        return an?.innerJoin ?: false
    }
}

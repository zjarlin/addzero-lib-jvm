package site.addzero.common.kt_util

import cn.hutool.core.collection.CollUtil
import cn.hutool.core.date.DateUtil
import cn.hutool.core.util.ObjUtil
import java.time.temporal.TemporalAccessor
import java.util.Date

object JlObjUtil {
    // 判断是否是日期字符串
    fun isDate(value: Any?): Boolean {
        return when (value) {
            is String -> try {
                DateUtil.parse(value)
                true
            } catch (e: Exception) {
                false
            }
            is Date, is TemporalAccessor -> true
            else -> false
        }
    }
}
fun Iterator<*>?.isEmpty(): Boolean {
    return CollUtil.isEmpty(this)
}

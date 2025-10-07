package site.addzero.util

import cn.hutool.core.date.DateUtil

// 判断是否是日期字符串
    fun isDateValue(value: Any?): Boolean {
        return when (value) {
            is String -> try {
                DateUtil.parse(value)
                true
            } catch (e: Exception) {
                false
            }

            is java.util.Date, is java.time.temporal.TemporalAccessor -> true
            else -> false
        }
    }

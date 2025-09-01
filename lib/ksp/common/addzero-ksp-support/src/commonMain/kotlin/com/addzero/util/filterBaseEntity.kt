package com.addzero.util

import com.addzero.context.SettingContext
import com.addzero.util.str.containsAny
import com.addzero.util.str.toLowCamelCase

fun filterBaseEntity(colName: String): Boolean {
    val settings = SettingContext.settings
    val id = settings.id
    val createBy = settings.createBy
    val updateBy = settings.updateBy
    val createTime = settings.createTime
    val updateTime = settings.updateTime

    if (colName.isNullOrEmpty()) {
        return false
    }
    val arrayOf = arrayOf(id, createBy, updateBy, createTime, updateTime)
    val arrayOf1 = arrayOf.map { it.toLowCamelCase() }.toTypedArray()
    arrayOf(id, createBy, updateBy, createTime, updateTime)
    val containsAny = colName.containsAny(*arrayOf)
    val containsAny1 = colName.containsAny(*arrayOf1)
    val b = !(containsAny || containsAny1)
    return b
}

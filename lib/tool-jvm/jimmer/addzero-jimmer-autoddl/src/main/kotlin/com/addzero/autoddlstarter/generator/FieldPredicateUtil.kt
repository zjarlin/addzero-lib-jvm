package com.addzero.autoddlstarter.generator

import cn.hutool.core.date.DateTime
import cn.hutool.core.util.StrUtil
import com.addzero.autoddlstarter.context.SettingContext
import com.addzero.autoddlstarter.generator.consts.DbType.POSTGRESQL
import com.addzero.autoddlstarter.generator.entity.JavaFieldMetaInfo
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

object FieldPredicateUtil {

    fun isType(f: JavaFieldMetaInfo, classes: Array<Class<*>>): Boolean {
        return classes.any { it.isAssignableFrom(f.type) }
    }

    fun isIntType(f: JavaFieldMetaInfo): Boolean {
        return isType(f, arrayOf(Int::class.java, Integer::class.java))
    }

    fun isLongType(f: JavaFieldMetaInfo): Boolean {
        return isType(f, arrayOf(Long::class.java))
    }

    /**
     * 长文本判断
     * @param [f]
     * @return [Boolean]
     */
    fun isTextType(f: JavaFieldMetaInfo): Boolean {


        val fieldName = f.name
        val javaType = f.type

        val isPg = SettingContext.settings.dbType == POSTGRESQL

        val assignableFrom = String::class.java.isAssignableFrom(javaType)

        if (isPg && assignableFrom) {
            return true
        }

        return StrUtil.containsAnyIgnoreCase(
            fieldName,
            "url",
            "base64",
            "text",
            "path",
            "introduction"
        ) && isType(f, arrayOf(String::class.java)) && assignableFrom
    }

    fun isStringType(f: JavaFieldMetaInfo): Boolean {
        return isType(f, arrayOf(String::class.java))

    }

    fun isCharType(f: JavaFieldMetaInfo): Boolean {
        return isType(f, arrayOf(Char::class.java))
    }

    fun isBooleanType(f: JavaFieldMetaInfo): Boolean {
        return isType(f, arrayOf(Boolean::class.java))
    }

    fun isDateType(f: JavaFieldMetaInfo): Boolean {
        return isType(f, arrayOf(Date::class.java, DateTime::class.java, LocalDate::class.java))
    }

    fun isTimeType(f: JavaFieldMetaInfo): Boolean {
        return isType(f, arrayOf(Date::class.java, DateTime::class.java, LocalTime::class.java))
    }

    fun isDateTimeType(f: JavaFieldMetaInfo): Boolean {
        return isType(f, arrayOf(Date::class.java, DateTime::class.java, LocalDateTime::class.java))
    }

    fun isBigDecimalType(f: JavaFieldMetaInfo): Boolean {
        return isType(f, arrayOf(BigDecimal::class.java))
    }

    fun isDoubleType(f: JavaFieldMetaInfo): Boolean {
        return isType(f, arrayOf(Double::class.java))
    }

}

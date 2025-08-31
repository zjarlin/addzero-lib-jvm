package com.addzero.kaleidoscope.util

import com.addzero.kaleidoscope.core.KldAnnotation
import com.addzero.kaleidoscope.core.KldAnnotationValue

/**
 * 注解值工具类
 */
object KldAnnotationValueUtils {

    /**
     * 将注解值转换为字符串
     */
    fun toString(value: KldAnnotationValue): String {
        return when (value) {
            is KldAnnotationValue.Primitive -> value.value.toString()
            is KldAnnotationValue.StringValueKld -> "\"${value.value}\""
            is KldAnnotationValue.KldEnumValue -> "${value.enumClass.typeName}.${value.value}"
            is KldAnnotationValue.KldClassValue -> "${value.qualifiedName}::class"
            is KldAnnotationValue.KldAnnotationInstance -> "@${value.value.qualifiedName}"
            is KldAnnotationValue.KldArrayValue -> "[${value.value.joinToString { toString(it) }}]"
            is KldAnnotationValue.KldUnknown -> value.value?.toString() ?: "null"
        }
    }

    /**
     * 获取字符串值
     */
    fun getStringValue(value: KldAnnotationValue): String? {
        return when (value) {
            is KldAnnotationValue.StringValueKld -> value.value
            is KldAnnotationValue.Primitive -> value.value.toString()
            else -> null
        }
    }

    /**
     * 获取布尔值
     */
    fun getBooleanValue(value: KldAnnotationValue): Boolean? {
        return when (value) {
            is KldAnnotationValue.Primitive -> value.value as? Boolean
            else -> null
        }
    }

    /**
     * 获取整数值
     */
    fun getIntValue(value: KldAnnotationValue): Int? {
        return when (value) {
            is KldAnnotationValue.Primitive -> value.value as? Int
            else -> null
        }
    }

    /**
     * 获取长整数值
     */
    fun getLongValue(value: KldAnnotationValue): Long? {
        return when (value) {
            is KldAnnotationValue.Primitive -> value.value as? Long
            else -> null
        }
    }

    /**
     * 获取浮点数值
     */
    fun getDoubleValue(value: KldAnnotationValue): Double? {
        return when (value) {
            is KldAnnotationValue.Primitive -> value.value as? Double
            else -> null
        }
    }

    /**
     * 获取类型值的全限定名称
     */
    fun getClassValue(value: KldAnnotationValue): String? {
        return when (value) {
            is KldAnnotationValue.KldClassValue -> value.qualifiedName
            else -> null
        }
    }

    /**
     * 获取类型值的简单名称
     */
    fun getClassSimpleName(value: KldAnnotationValue): String? {
        return when (value) {
            is KldAnnotationValue.KldClassValue -> value.simpleName
            else -> null
        }
    }

    /**
     * 获取枚举值
     */
    fun getEnumValue(value: KldAnnotationValue): String? {
        return when (value) {
            is KldAnnotationValue.KldEnumValue -> value.value
            else -> null
        }
    }

    /**
     * 获取数组值
     */
    fun getArrayValue(value: KldAnnotationValue): List<KldAnnotationValue>? {
        return when (value) {
            is KldAnnotationValue.KldArrayValue -> value.value
            else -> null
        }
    }

    /**
     * 获取注解值
     */
    fun getAnnotationValue(value: KldAnnotationValue): KldAnnotation? {
        return when (value) {
            is KldAnnotationValue.KldAnnotationInstance -> value.value
            else -> null
        }
    }
}

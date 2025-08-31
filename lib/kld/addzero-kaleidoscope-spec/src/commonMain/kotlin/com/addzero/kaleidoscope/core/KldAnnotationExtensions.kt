package com.addzero.kaleidoscope.core

/**
 * 从注解中获取字符串值
 *
 * @param parameterName 参数名称
 * @return 字符串值，如果不存在或不是字符串类型则返回空字符串
 */
fun KldAnnotation.getAnnotationStringValue(parameterName: String): String {
    return try {
        val argument = this.argumentList.find { it.name == parameterName }
        argument?.value?.let { value ->
            when (value) {
                is KldAnnotationValue.StringValueKld -> value.value
                is KldAnnotationValue.Primitive -> value.value.toString()
                else -> value.toString()
            }
        }?.removeSurrounding("\"") // 移除引号
            ?: ""
    } catch (e: Exception) {
        ""
    }
}

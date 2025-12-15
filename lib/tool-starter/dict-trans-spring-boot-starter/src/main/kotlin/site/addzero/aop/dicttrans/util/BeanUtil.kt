package site.addzero.aop.dicttrans.util

import java.lang.reflect.Field

/**
 * Bean工具类，替代hutool的BeanUtil
 *
 * @author zjarlin
 */
internal object BeanUtil {

    /**
     * 复制属性
     */
    fun copyProperties(source: Any, target: Any) {
        val sourceClass = source.javaClass
        val targetClass = target.javaClass

        val sourceFields = ReflectUtil.getFields(sourceClass)
        val targetFields = ReflectUtil.getFields(targetClass)

        // 创建目标字段映射
        val targetFieldMap = targetFields.associateBy { it.name }

        sourceFields.forEach { sourceField ->
            val targetField = targetFieldMap[sourceField.name]
            if (targetField != null && isAssignable(sourceField.type, targetField.type)) {
                try {
                    val value = ReflectUtil.getFieldValue(source, sourceField)
                    ReflectUtil.setFieldValue(target, targetField, value)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 忽略复制失败的字段
                }
            }
        }
    }

    /**
     * 判断类型是否可赋值
     */
    private fun isAssignable(sourceType: Class<*>, targetType: Class<*>): Boolean {
        return targetType.isAssignableFrom(sourceType) ||
               (sourceType.isPrimitive && targetType == getWrapperType(sourceType)) ||
               (targetType.isPrimitive && sourceType == getWrapperType(targetType))
    }

    /**
     * 获取基本类型的包装类型
     */
    private fun getWrapperType(primitiveType: Class<*>): Class<*> {
        return when (primitiveType) {
            Boolean::class.javaPrimitiveType -> Boolean::class.javaObjectType
            Byte::class.javaPrimitiveType -> Byte::class.javaObjectType
            Char::class.javaPrimitiveType -> Char::class.javaObjectType
            Short::class.javaPrimitiveType -> Short::class.javaObjectType
            Int::class.javaPrimitiveType -> Int::class.javaObjectType
            Long::class.javaPrimitiveType -> Long::class.javaObjectType
            Float::class.javaPrimitiveType -> Float::class.javaObjectType
            Double::class.javaPrimitiveType -> Double::class.javaObjectType
            else -> primitiveType
        }
    }
}

package site.addzero.tool.bean

import site.addzero.util.ImprovedReflectUtil

/** Utility for shallow property copy between Kotlin/Java beans. */
object BeanUtil {
    fun copyProperties(source: Any, target: Any) {
        val sourceFields = ImprovedReflectUtil.getFields(source.javaClass)
        val targetFields = ImprovedReflectUtil.getFields(target.javaClass).associateBy { it.name }

        sourceFields.forEach { field ->
            val targetField = targetFields[field.name] ?: return@forEach
            if (!isAssignable(field.type, targetField.type)) return@forEach
            val value = ImprovedReflectUtil.getFieldValue(source, field) ?: return@forEach
            ImprovedReflectUtil.setFieldValue(target, targetField, value)
        }
    }

    private fun isAssignable(source: Class<*>, target: Class<*>): Boolean {
        return target.isAssignableFrom(source) ||
            (source.isPrimitive && target == wrapperOf(source)) ||
            (target.isPrimitive && source == wrapperOf(target))
    }

    private fun wrapperOf(primitive: Class<*>): Class<*> = when (primitive) {
        Boolean::class.javaPrimitiveType -> Boolean::class.javaObjectType
        Byte::class.javaPrimitiveType -> Byte::class.javaObjectType
        Char::class.javaPrimitiveType -> Char::class.javaObjectType
        Short::class.javaPrimitiveType -> Short::class.javaObjectType
        Int::class.javaPrimitiveType -> Int::class.javaObjectType
        Long::class.javaPrimitiveType -> Long::class.javaObjectType
        Float::class.javaPrimitiveType -> Float::class.javaObjectType
        Double::class.javaPrimitiveType -> Double::class.javaObjectType
        else -> primitive
    }
}

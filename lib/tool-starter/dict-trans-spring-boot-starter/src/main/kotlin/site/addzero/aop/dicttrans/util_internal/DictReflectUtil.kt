package site.addzero.aop.dicttrans.util_internal

import site.addzero.util.ImprovedReflectUtil

/**
 * 字典翻译反射写入工具。
 */
internal object DictReflectUtil {
    fun setFieldValue(target: Any?, fieldName: String, value: Any?) {
        if (target == null || fieldName.isBlank()) {
            return
        }

        if (target is MutableMap<*, *>) {
            @Suppress("UNCHECKED_CAST")
            (target as MutableMap<String, Any?>)[fieldName] = value
            return
        }

        val field = ImprovedReflectUtil.getFields(target.javaClass)
            .firstOrNull { it.name == fieldName }
            ?: return
        ImprovedReflectUtil.setFieldValue(target, field, value)
    }
}

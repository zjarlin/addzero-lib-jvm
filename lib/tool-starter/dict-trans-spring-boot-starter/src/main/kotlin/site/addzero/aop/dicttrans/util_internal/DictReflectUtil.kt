package site.addzero.aop.dicttrans.util_internal

import site.addzero.util.ImprovedReflectUtil

/**
 * 字典翻译反射写入工具。
 */
internal object DictReflectUtil {
        val field = ImprovedReflectUtil.getFields(target.javaClass)
            .firstOrNull { it.name == fieldName }
            ?: return
        ImprovedReflectUtil.setFieldValue(target, field, value)
    }

}

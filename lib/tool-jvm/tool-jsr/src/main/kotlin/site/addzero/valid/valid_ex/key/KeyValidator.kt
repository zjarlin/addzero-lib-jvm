package site.addzero.valid.valid_ex.key

import cn.hutool.extra.spring.SpringUtil
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import java.lang.reflect.Field

/**
 * Key注解的校验器
 *
 * @author zjarlin
 * @since 2025/10/22
 */
class KeyValidator : ConstraintValidator<Key, Any?> {

    private lateinit var keyAnnotation: Key

    override fun initialize(constraintAnnotation: Key) {
        this.keyAnnotation = constraintAnnotation
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
        // value 是当前标记了 @Key 注解的对象或字段的值
        // 当 @Key 标记在类上时，value 是整个对象
        // 当 @Key 标记在字段上时，value 是字段的值

        // 如果值为null，则不进行唯一性校验
        if (value == null) {
            return true
        }

        // 获取当前正在校验的对象
        val currentObject = value

        // 获取当前对象类及所有父类中标记了[@Key](file:///Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/tool-jvm/tool-jsr/src/main/kotlin/site/addzero/valid/valid_ex/key/Key.kt#L18-L25)注解且属于当前group的字段
        val keyFields = getKeyFields(currentObject.javaClass, keyAnnotation.group)
        if (keyFields.isEmpty()) {
            return true
        }

        // 收集字段值
        val fieldValues = mutableMapOf<String, Any?>()
        val clazz = currentObject.javaClass
        for (field in keyFields) {
            field.isAccessible = true
            val fieldValue = field.get(currentObject)
            fieldValues[field.name] = fieldValue
        }

        // 如果有任何字段值为null，则不进行唯一性校验
        if (fieldValues.any { it.value == null }) {
            return true
        }

        // 获取校验器实例并执行校验
        val validator = getKeyUniqueValidator()
        val tableName = getTableName(clazz)

        // 获取排除ID（用于更新场景）
        val excludeId = getIdValue(currentObject)

        val isUnique = validator.isUnique(tableName, keyAnnotation.group, fieldValues, excludeId)

        // 如果校验失败，构建错误消息
        if (!isUnique) {
            val message = keyAnnotation.message.ifEmpty { buildErrorMessage(keyFields, fieldValues) }
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation()
            return false
        }

        return true
    }

    private fun buildErrorMessage(keyFields: List<Field>, fieldValues: Map<String, Any?>): String {
        val fieldNames = keyFields.joinToString(", ") { it.name }
        val fieldValuesStr = fieldValues.entries.joinToString(", ") { "${it.key}=${it.value}" }
        return "字段组合 ($fieldNames) 的值 ($fieldValuesStr) 已存在"
    }

    private fun getKeyUniqueValidator(): KeyUniqueValidator {
         return SpringUtil.getBean(KeyUniqueValidator::class.java)
    }

    private fun getKeyFields(clazz: Class<*>, group: String): List<Field> {
        val fields = mutableListOf<Field>()

        // 遍历当前类及所有父类
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            for (field in currentClass.declaredFields) {
                val keyAnnotation = field.getAnnotation(Key::class.java)
                if (keyAnnotation != null && keyAnnotation.group == group) {
                    fields.add(field)
                }
            }
            currentClass = currentClass.superclass
        }

        return fields
    }

    private fun getTableName(clazz: Class<*>): String {
        // 这里应该根据实体类获取对应表名的逻辑
        // 示例：可以使用JPA的[@Table](file:///Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/tool-spring/src/main/kotlin/site/addzero/web/infra/advice/RespBodyAdviceConfig.kt#L22-L23)注解或其他方式
        return clazz.simpleName ?: "unknown_table"
    }

    private fun getIdValue(obj: Any): Any? {
        // 这里应该获取实体对象的ID值的逻辑
        // 示例：可以查找标记了[@Id](file:///Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/tool-jvm/tool-jsr/src/main/kotlin/site/addzero/valid/valid_ex/key/Key.kt#L18-L25)注解的字段或者名为id的字段
        var clazz: Class<*>? = obj.javaClass
        while (clazz != null) {
            val idField = clazz.declaredFields.find {
                it.name.equals("id", ignoreCase = true)
            }
            if (idField != null) {
                idField.isAccessible = true
                return idField.get(obj)
            }
            clazz = clazz.superclass
        }
        return null
    }
}

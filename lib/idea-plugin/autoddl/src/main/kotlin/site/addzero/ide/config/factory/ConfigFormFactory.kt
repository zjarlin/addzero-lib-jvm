package site.addzero.ide.config.factory

import site.addzero.ide.config.annotation.*
import site.addzero.ide.config.model.*
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

/**
 * 配置表单工厂类，用于根据注解和数据类生成配置模型
 */
object ConfigFormFactory {
    
    /**
     * 根据带注解的数据类生成配置项列表
     *
     * @param configClass 带有配置注解的数据类
     * @return 配置项列表
     */
    fun generateConfigItems(configClass: KClass<*>): List<ConfigItem> {
        val configItems = mutableListOf<ConfigItem>()
        
        // 检查类是否有 @Configurable 注解
        val classAnnotation = configClass.findAnnotation<Configurable>()
        if (classAnnotation == null) {
            System.err.println("Class ${configClass.simpleName} is not annotated with @Configurable")
            // 不抛出异常，而是返回空列表
            return emptyList()
        }
        
        // 遍历类的所有属性
        configClass.memberProperties.forEach { property ->
            val field = property.javaField
            if (field != null) {
                try {
                    val configItem = createConfigItemFromField(field, configClass, property.name)
                    if (configItem != null) {
                        configItems.add(configItem)
                    }
                } catch (e: Exception) {
                    System.err.println("Failed to create config item for field ${property.name} in ${configClass.simpleName}: ${e.message}")
                }
            }
        }
        
        System.out.println("Generated ${configItems.size} config items for ${configClass.simpleName}")
        return configItems
    }
    
    /**
     * 根据字段创建配置项
     */
    private fun createConfigItemFromField(field: Field, declaringClass: KClass<*>, propertyName: String): ConfigItem? {
        // 构建默认的key，格式为"类名.字段名"
        val defaultKey = "${declaringClass.simpleName}.$propertyName"
        
        // 检查 @ConfigField 注解
        val configField = field.getAnnotation(ConfigField::class.java)
        if (configField != null) {
            return KeyValueConfig(
                key = if (configField.key.isNotEmpty()) configField.key else defaultKey,
                label = if (configField.label.isNotEmpty()) configField.label else propertyName,
                description = if (configField.description.isNotEmpty()) configField.description else null,
                defaultValue = field.get(null)?.toString(),
                required = configField.required,
                inputType = configField.inputType
            )
        }
        
        // 检查 @ConfigSelect 注解
        val configSelect = field.getAnnotation(ConfigSelect::class.java)
        if (configSelect != null) {
            val options = configSelect.options.map { 
                site.addzero.ide.config.model.SelectOption(it.value, it.label) 
            }
            
            return SelectConfig(
                key = if (configSelect.key.isNotEmpty()) configSelect.key else defaultKey,
                label = if (configSelect.label.isNotEmpty()) configSelect.label else propertyName,
                description = if (configSelect.description.isNotEmpty()) configSelect.description else null,
                options = options,
                defaultValue = field.get(null)?.toString(),
                required = configSelect.required
            )
        }
        
        // 检查 @ConfigCheckbox 注解
        val configCheckbox = field.getAnnotation(ConfigCheckbox::class.java)
        if (configCheckbox != null) {
            val defaultValue = field.get(null) as? Boolean ?: false
            
            return CheckboxConfig(
                key = if (configCheckbox.key.isNotEmpty()) configCheckbox.key else defaultKey,
                label = if (configCheckbox.label.isNotEmpty()) configCheckbox.label else propertyName,
                description = if (configCheckbox.description.isNotEmpty()) configCheckbox.description else null,
                defaultValue = defaultValue,
                required = false
            )
        }
        
        // 检查 @ConfigList 注解
        val configList = field.getAnnotation(ConfigList::class.java)
        if (configList != null) {
            // 这里需要根据字段类型处理列表项模板
            // 简化实现，实际项目中可能需要更复杂的处理
            return ListConfig(
                key = if (configList.key.isNotEmpty()) configList.key else defaultKey,
                label = if (configList.label.isNotEmpty()) configList.label else propertyName,
                description = if (configList.description.isNotEmpty()) configList.description else null,
                itemTemplate = listOf(), // 简化处理，实际应根据字段类型生成模板
                maxItems = if (configList.maxItems >= 0) configList.maxItems else null,
                minItems = if (configList.minItems >= 0) configList.minItems else null,
                required = false
            )
        }
        
        // 检查 @ConfigTable 注解
        val configTable = field.getAnnotation(ConfigTable::class.java)
        if (configTable != null) {
            // 简化处理，实际应根据字段类型生成列定义
            return TableConfig(
                key = if (configTable.key.isNotEmpty()) configTable.key else defaultKey,
                label = if (configTable.label.isNotEmpty()) configTable.label else propertyName,
                description = if (configTable.description.isNotEmpty()) configTable.description else null,
                columns = listOf(), // 简化处理，实际应根据字段类型生成列定义
                maxRows = if (configTable.maxRows >= 0) configTable.maxRows else null,
                minRows = if (configTable.minRows >= 0) configTable.minRows else null,
                required = false
            )
        }
        
        // 检查 @ConfigConditional 注解
        val configConditional = field.getAnnotation(ConfigConditional::class.java)
        if (configConditional != null) {
            // 获取实际的配置项
            val actualConfigItem = createConfigItemFromField(field, declaringClass, propertyName)
            if (actualConfigItem != null) {
                val operator = when (configConditional.conditionOperator.uppercase()) {
                    "EQUALS" -> ConditionOperator.EQUALS
                    "NOT_EQUALS" -> ConditionOperator.NOT_EQUALS
                    "CONTAINS" -> ConditionOperator.CONTAINS
                    "NOT_CONTAINS" -> ConditionOperator.NOT_CONTAINS
                    "GREATER_THAN" -> ConditionOperator.GREATER_THAN
                    "LESS_THAN" -> ConditionOperator.LESS_THAN
                    else -> ConditionOperator.EQUALS
                }
                
                val condition = Condition(
                    field = configConditional.conditionField,
                    operator = operator,
                    value = configConditional.conditionValue
                )
                
                return ConditionalConfig(
                    key = if (configConditional.key.isNotEmpty()) configConditional.key else defaultKey,
                    label = if (configConditional.label.isNotEmpty()) configConditional.label else propertyName,
                    description = if (configConditional.description.isNotEmpty()) configConditional.description else null,
                    condition = condition,
                    configItem = actualConfigItem,
                    required = false
                )
            }
        }
        
        return null
    }
    
    /**
     * 从示例对象创建配置项（支持默认值）
     */
    fun generateConfigItems(configClass: KClass<*>, exampleObject: Any): List<ConfigItem> {
        val configItems = mutableListOf<ConfigItem>()
        
        // 检查类是否有 @Configurable 注解
        val classAnnotation = configClass.findAnnotation<Configurable>()
        if (classAnnotation == null) {
            throw IllegalArgumentException("Class must be annotated with @Configurable")
        }
        
        // 遍历类的所有属性
        configClass.memberProperties.forEach { property ->
            val field = property.javaField
            if (field != null) {
                val defaultValue = property.getter.call(exampleObject)?.toString()
                val configItem = createConfigItemFromFieldWithDefault(field, configClass, defaultValue, property.name)
                if (configItem != null) {
                    configItems.add(configItem)
                }
            }
        }
        
        return configItems
    }
    
    /**
     * 根据字段和默认值创建配置项
     */
    private fun createConfigItemFromFieldWithDefault(
        field: Field, 
        declaringClass: KClass<*>, 
        defaultValue: String?,
        propertyName: String
    ): ConfigItem? {
        // 构建默认的key，格式为"类名.字段名"
        val defaultKey = "${declaringClass.simpleName}.$propertyName"
        
        // 检查 @ConfigField 注解
        val configField = field.getAnnotation(ConfigField::class.java)
        if (configField != null) {
            return KeyValueConfig(
                key = if (configField.key.isNotEmpty()) configField.key else defaultKey,
                label = if (configField.label.isNotEmpty()) configField.label else propertyName,
                description = if (configField.description.isNotEmpty()) configField.description else null,
                defaultValue = defaultValue,
                required = configField.required,
                inputType = configField.inputType
            )
        }
        
        // 检查 @ConfigSelect 注解
        val configSelect = field.getAnnotation(ConfigSelect::class.java)
        if (configSelect != null) {
            val options = configSelect.options.map { 
                site.addzero.ide.config.model.SelectOption(it.value, it.label) 
            }
            
            return SelectConfig(
                key = if (configSelect.key.isNotEmpty()) configSelect.key else defaultKey,
                label = if (configSelect.label.isNotEmpty()) configSelect.label else propertyName,
                description = if (configSelect.description.isNotEmpty()) configSelect.description else null,
                options = options,
                defaultValue = defaultValue,
                required = configSelect.required
            )
        }
        
        // 检查 @ConfigCheckbox 注解
        val configCheckbox = field.getAnnotation(ConfigCheckbox::class.java)
        if (configCheckbox != null) {
            val boolValue = defaultValue?.toBoolean() ?: false
            
            return CheckboxConfig(
                key = if (configCheckbox.key.isNotEmpty()) configCheckbox.key else defaultKey,
                label = if (configCheckbox.label.isNotEmpty()) configCheckbox.label else propertyName,
                description = if (configCheckbox.description.isNotEmpty()) configCheckbox.description else null,
                defaultValue = boolValue,
                required = false
            )
        }
        
        // 检查 @ConfigList 注解
        val configList = field.getAnnotation(ConfigList::class.java)
        if (configList != null) {
            return ListConfig(
                key = if (configList.key.isNotEmpty()) configList.key else defaultKey,
                label = if (configList.label.isNotEmpty()) configList.label else propertyName,
                description = if (configList.description.isNotEmpty()) configList.description else null,
                itemTemplate = listOf(),
                maxItems = if (configList.maxItems >= 0) configList.maxItems else null,
                minItems = if (configList.minItems >= 0) configList.minItems else null,
                required = false
            )
        }
        
        // 检查 @ConfigTable 注解
        val configTable = field.getAnnotation(ConfigTable::class.java)
        if (configTable != null) {
            return TableConfig(
                key = if (configTable.key.isNotEmpty()) configTable.key else defaultKey,
                label = if (configTable.label.isNotEmpty()) configTable.label else propertyName,
                description = if (configTable.description.isNotEmpty()) configTable.description else null,
                columns = listOf(),
                maxRows = if (configTable.maxRows >= 0) configTable.maxRows else null,
                minRows = if (configTable.minRows >= 0) configTable.minRows else null,
                required = false
            )
        }
        
        // 检查 @ConfigConditional 注解
        val configConditional = field.getAnnotation(ConfigConditional::class.java)
        if (configConditional != null) {
            // 获取实际的配置项
            val actualConfigItem = createConfigItemFromFieldWithDefault(field, declaringClass, defaultValue, propertyName)
            if (actualConfigItem != null) {
                val operator = when (configConditional.conditionOperator.uppercase()) {
                    "EQUALS" -> ConditionOperator.EQUALS
                    "NOT_EQUALS" -> ConditionOperator.NOT_EQUALS
                    "CONTAINS" -> ConditionOperator.CONTAINS
                    "NOT_CONTAINS" -> ConditionOperator.NOT_CONTAINS
                    "GREATER_THAN" -> ConditionOperator.GREATER_THAN
                    "LESS_THAN" -> ConditionOperator.LESS_THAN
                    else -> ConditionOperator.EQUALS
                }
                
                val condition = Condition(
                    field = configConditional.conditionField,
                    operator = operator,
                    value = configConditional.conditionValue
                )
                
                return ConditionalConfig(
                    key = if (configConditional.key.isNotEmpty()) configConditional.key else defaultKey,
                    label = if (configConditional.label.isNotEmpty()) configConditional.label else propertyName,
                    description = if (configConditional.description.isNotEmpty()) configConditional.description else null,
                    condition = condition,
                    configItem = actualConfigItem,
                    required = false
                )
            }
        }
        
        return null
    }
}
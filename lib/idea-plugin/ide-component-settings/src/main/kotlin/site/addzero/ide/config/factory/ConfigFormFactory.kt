package site.addzero.ide.config.factory

import site.addzero.ide.config.annotation.*
import site.addzero.ide.config.model.ConfigItem
import site.addzero.ide.config.model.InputType
import site.addzero.ide.config.model.SelectOption
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * 配置表单工厂类
 * 负责根据配置类生成配置项列表
 */
object ConfigFormFactory {
    
    /**
     * 根据配置类生成配置项列表
     *
     * @param configClass 配置类
     * @return 配置项列表
     */
    fun generateConfigItems(configClass: KClass<*>): List<ConfigItem> {
        val configItems = mutableListOf<ConfigItem>()
        
        // 获取类的所有属性
        configClass.memberProperties.forEach { property ->
            val configItem = createConfigItem(property, configClass)
            if (configItem != null) {
                configItems.add(configItem)
            }
        }
        
        return configItems
    }
    
    /**
     * 根据属性创建配置项
     */
    private fun createConfigItem(property: KProperty1<*, *>, configClass: KClass<*>): ConfigItem? {
        // 检查是否有 @ConfigField 注解
        val configField = property.annotations.find { it is ConfigField } as? ConfigField
        if (configField != null) {
            return ConfigItem(
                key = if (configField.key.isNotEmpty()) configField.key else property.name,
                label = if (configField.label.isNotEmpty()) configField.label else property.name,
                description = configField.description,
                required = configField.required,
                inputType = configField.inputType,
                options = emptyList()
            )
        }
        
        // 检查是否有 @ConfigSelect 注解
        val configSelect = property.annotations.find { it is ConfigSelect } as? ConfigSelect
        if (configSelect != null) {
            // 创建选项列表
            val options = if (configSelect.optionsValue.size == configSelect.optionsLabel.size) {
                configSelect.optionsValue.zip(configSelect.optionsLabel) { value, label ->
                    SelectOption(value, label)
                }
            } else {
                emptyList()
            }
            
            return ConfigItem(
                key = if (configSelect.key.isNotEmpty()) configSelect.key else property.name,
                label = if (configSelect.label.isNotEmpty()) configSelect.label else property.name,
                description = configSelect.description,
                required = configSelect.required,
                inputType = InputType.SELECT,
                options = options
            )
        }
        
        // 检查是否有 @ConfigCheckbox 注解
        val configCheckbox = property.annotations.find { it is ConfigCheckbox } as? ConfigCheckbox
        if (configCheckbox != null) {
            return ConfigItem(
                key = if (configCheckbox.key.isNotEmpty()) configCheckbox.key else property.name,
                label = if (configCheckbox.label.isNotEmpty()) configCheckbox.label else property.name,
                description = configCheckbox.description,
                required = false,
                inputType = InputType.CHECKBOX,
                options = emptyList()
            )
        }
        
        // 默认情况下，为没有注解的属性创建文本输入项
        return ConfigItem(
            key = property.name,
            label = property.name,
            description = "",
            required = false,
            inputType = InputType.TEXT,
            options = emptyList()
        )
    }
}
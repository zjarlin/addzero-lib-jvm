package site.addzero.ide.config.factory

import site.addzero.ide.config.annotation.*
import site.addzero.ide.config.model.ConfigItem
import site.addzero.ide.config.model.InputType
import site.addzero.ide.config.model.SelectOption
import site.addzero.ide.config.model.TableColumn
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure

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
        
        // 获取构造函数参数的注解映射（对于data class，注解在构造函数参数上）
        val constructorParamAnnotations = mutableMapOf<String, List<Annotation>>()
        val primaryConstructor = configClass.primaryConstructor
        
        // 按构造函数参数顺序获取属性，保证顺序
        if (primaryConstructor != null) {
            // 先收集构造函数参数信息
            primaryConstructor.parameters.forEach { param ->
                if (param.name != null) {
                    constructorParamAnnotations[param.name!!] = param.annotations
                }
            }
            
            // 按构造函数参数顺序处理属性（保证顺序）
            primaryConstructor.parameters.forEach { param ->
                if (param.name != null) {
                    // 找到对应的属性
                    val property = configClass.memberProperties.find { it.name == param.name }
                    if (property != null) {
                        val configItem = createConfigItem(property, configClass, constructorParamAnnotations)
                        if (configItem != null) {
                            configItems.add(configItem)
                        }
                    }
                }
            }
        } else {
            // 如果没有主构造函数，则按成员属性顺序处理
            configClass.memberProperties.forEach { property ->
                val configItem = createConfigItem(property, configClass, constructorParamAnnotations)
                if (configItem != null) {
                    configItems.add(configItem)
                }
            }
        }
        
        return configItems
    }
    
    /**
     * 根据属性创建配置项
     * 注意：对于data class，注解可能在构造函数参数上，需要通过构造函数参数获取
     */
    private fun createConfigItem(
        property: KProperty1<*, *>, 
        configClass: KClass<*>,
        constructorParamAnnotations: Map<String, List<Annotation>>
    ): ConfigItem? {
        val propertyName = property.name
        val returnType = property.returnType
        
        // 首先尝试从构造函数参数获取注解（对于data class）
        val paramAnnotations = constructorParamAnnotations[propertyName] ?: emptyList()
        
        @Suppress("UNCHECKED_CAST")
        var configTable = property.findAnnotation<ConfigTable>() 
            ?: paramAnnotations.find { it is ConfigTable } as? ConfigTable
        
        @Suppress("UNCHECKED_CAST")
        var configCheckbox = property.findAnnotation<ConfigCheckbox>()
            ?: paramAnnotations.find { it is ConfigCheckbox } as? ConfigCheckbox
        
        @Suppress("UNCHECKED_CAST")
        var configSelect = property.findAnnotation<ConfigSelect>()
            ?: paramAnnotations.find { it is ConfigSelect } as? ConfigSelect
        
        @Suppress("UNCHECKED_CAST")
        var configField = property.findAnnotation<ConfigField>()
            ?: paramAnnotations.find { it is ConfigField } as? ConfigField
        
        // 如果仍然无法获取，尝试通过Java字段获取
        if (configTable == null && configCheckbox == null && configSelect == null && configField == null) {
            val javaField = property.javaField
            if (javaField != null) {
                configTable = javaField.getAnnotation(ConfigTable::class.java)
                configCheckbox = javaField.getAnnotation(ConfigCheckbox::class.java)
                configSelect = javaField.getAnnotation(ConfigSelect::class.java)
                configField = javaField.getAnnotation(ConfigField::class.java)
            }
        }
        
        // 检查是否有 @ConfigTable 注解（优先检查，因为可能有其他注解）
        if (configTable != null) {
            // 从List泛型中提取元素类型
            val tableColumns = extractTableColumns(returnType)
            
            println("ConfigFormFactory: Found @ConfigTable for property $propertyName, columns: ${tableColumns.size}")
            
            return ConfigItem(
                key = if (configTable.key.isNotEmpty()) configTable.key else propertyName,
                label = if (configTable.label.isNotEmpty()) configTable.label else propertyName,
                description = configTable.description,
                required = false,
                inputType = InputType.TABLE,
                options = emptyList(),
                tableColumns = tableColumns,
                minRows = configTable.minRows,
                maxRows = configTable.maxRows
            )
        }
        
        // 检查是否有 @ConfigCheckbox 注解
        if (configCheckbox != null) {
            println("ConfigFormFactory: Found @ConfigCheckbox for property $propertyName")
            return ConfigItem(
                key = if (configCheckbox.key.isNotEmpty()) configCheckbox.key else propertyName,
                label = if (configCheckbox.label.isNotEmpty()) configCheckbox.label else propertyName,
                description = configCheckbox.description,
                required = false,
                inputType = InputType.CHECKBOX,
                options = emptyList()
            )
        }
        
        // 检查是否有 @ConfigSelect 注解
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
                key = if (configSelect.key.isNotEmpty()) configSelect.key else propertyName,
                label = if (configSelect.label.isNotEmpty()) configSelect.label else propertyName,
                description = configSelect.description,
                required = configSelect.required,
                inputType = InputType.SELECT,
                options = options
            )
        }
        
        // 检查是否有 @ConfigField 注解
        if (configField != null) {
            return ConfigItem(
                key = if (configField.key.isNotEmpty()) configField.key else propertyName,
                label = if (configField.label.isNotEmpty()) configField.label else propertyName,
                description = configField.description,
                required = configField.required,
                inputType = configField.inputType,
                options = emptyList()
            )
        }
        
        // 默认情况下，为没有注解的属性创建文本输入项
        println("ConfigFormFactory: No annotation found for property $propertyName, using default TEXT input")
        return ConfigItem(
            key = propertyName,
            label = propertyName,
            description = "",
            required = false,
            inputType = InputType.TEXT,
            options = emptyList()
        )
    }
    
    /**
     * 从List泛型类型中提取表格列配置
     */
    private fun extractTableColumns(propertyType: KType): List<TableColumn> {
        // 检查是否是List类型 - 使用jvmErasure来检查
        val typeClass = propertyType.jvmErasure
        if (typeClass.qualifiedName == "kotlin.collections.List" || 
            typeClass.qualifiedName == "java.util.List") {
            // 获取泛型参数类型
            val typeArguments = propertyType.arguments
            if (typeArguments.isNotEmpty()) {
                val elementType = typeArguments[0].type
                if (elementType != null) {
                    val elementClass = elementType.jvmErasure
                    // 从元素类中提取属性作为表格列
                    return extractColumnsFromClass(elementClass)
                }
            }
        }
        return emptyList()
    }
    
    /**
     * 从数据类中提取列配置
     */
    private fun extractColumnsFromClass(elementClass: KClass<*>): List<TableColumn> {
        val columns = mutableListOf<TableColumn>()
        
        // 获取构造函数参数的注解映射（对于data class，注解在构造函数参数上）
        val constructorParamAnnotations = mutableMapOf<String, List<Annotation>>()
        val primaryConstructor = elementClass.primaryConstructor
        
        // 按构造函数参数顺序处理，保证列的顺序
        if (primaryConstructor != null) {
            // 先收集构造函数参数信息
            primaryConstructor.parameters.forEach { param ->
                if (param.name != null) {
                    constructorParamAnnotations[param.name!!] = param.annotations
                }
            }
            
            // 按构造函数参数顺序处理属性（保证顺序）
            primaryConstructor.parameters.forEach { param ->
                if (param.name != null) {
                    // 找到对应的属性
                    val prop = elementClass.memberProperties.find { it.name == param.name }
                    if (prop != null) {
                        val propertyName = prop.name
                        
                        // 首先尝试从构造函数参数获取注解（对于data class）
                        val paramAnnotations = constructorParamAnnotations[propertyName] ?: emptyList()
                        
                        @Suppress("UNCHECKED_CAST")
                        val configField = prop.findAnnotation<ConfigField>()
                            ?: paramAnnotations.find { it is ConfigField } as? ConfigField
                        
                        @Suppress("UNCHECKED_CAST")
                        val configSelect = prop.findAnnotation<ConfigSelect>()
                            ?: paramAnnotations.find { it is ConfigSelect } as? ConfigSelect
                        
                        @Suppress("UNCHECKED_CAST")
                        val configCheckbox = prop.findAnnotation<ConfigCheckbox>()
                            ?: paramAnnotations.find { it is ConfigCheckbox } as? ConfigCheckbox
                        
                        val label = when {
                            configField != null && configField.label.isNotEmpty() -> configField.label
                            configSelect != null && configSelect.label.isNotEmpty() -> configSelect.label
                            configCheckbox != null && configCheckbox.label.isNotEmpty() -> configCheckbox.label
                            else -> propertyName
                        }
                        
                        val inputType = when {
                            configField != null -> configField.inputType
                            configSelect != null -> InputType.SELECT
                            configCheckbox != null -> InputType.CHECKBOX
                            else -> InputType.TEXT
                        }
                        
                        println("ConfigFormFactory: Extracting column from $propertyName: label=$label, inputType=$inputType")
                        
                        columns.add(TableColumn(
                            key = propertyName,
                            label = label,
                            inputType = inputType,
                            width = 150
                        ))
                    }
                }
            }
        } else {
            // 如果没有主构造函数，则按成员属性顺序处理
            elementClass.memberProperties.forEach { prop ->
                val propertyName = prop.name
                
                @Suppress("UNCHECKED_CAST")
                val configField = prop.findAnnotation<ConfigField>()
                
                @Suppress("UNCHECKED_CAST")
                val configSelect = prop.findAnnotation<ConfigSelect>()
                
                @Suppress("UNCHECKED_CAST")
                val configCheckbox = prop.findAnnotation<ConfigCheckbox>()
                
                val label = when {
                    configField != null && configField.label.isNotEmpty() -> configField.label
                    configSelect != null && configSelect.label.isNotEmpty() -> configSelect.label
                    configCheckbox != null && configCheckbox.label.isNotEmpty() -> configCheckbox.label
                    else -> propertyName
                }
                
                val inputType = when {
                    configField != null -> configField.inputType
                    configSelect != null -> InputType.SELECT
                    configCheckbox != null -> InputType.CHECKBOX
                    else -> InputType.TEXT
                }
                
                columns.add(TableColumn(
                    key = propertyName,
                    label = label,
                    inputType = inputType,
                    width = 150
                ))
            }
        }
        
        return columns
    }
}
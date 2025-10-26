package site.addzero.ide.ui.form

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import site.addzero.ide.config.model.*
import java.lang.reflect.InvocationTargetException
import javax.swing.JComponent
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * 动态表单构建器，根据配置项列表生成 IntelliJ UI DSL 表单
 */
class DynamicFormBuilder {
    private val components = mutableMapOf<String, JComponent>()
    private val configItemsMap = mutableMapOf<String, ConfigItem>()
    private var originalData = mutableMapOf<String, Any?>()

    /**
     * 根据配置项列表构建表单面板
     *
     * @param configItems 配置项列表
     * @return 构建的表单面板
     */
    fun buildFormPanel(configItems: List<ConfigItem>): DialogPanel {
        return panel {
            if (configItems.isEmpty()) {
                row {
                    label("该配置项没有可编辑的参数")
                }
            } else {
                configItems.forEach { configItem ->
                    try {
                        // 存储配置项以便后续使用
                        configItemsMap[configItem.key] = configItem

                        when (configItem) {
                            is KeyValueConfig -> {
                                addKeyValueField(configItem)
                            }
                            is SelectConfig -> {
                                addSelectField(configItem)
                            }
                            is CheckboxConfig -> {
                                addCheckboxField(configItem)
                            }
                            is ListConfig -> {
                                addListField(configItem)
                            }
                            is TableConfig -> {
                                addTableField(configItem)
                            }
                            is ConditionalConfig -> {
                                addConditionalField(configItem)
                            }
                        }
                    } catch (e: Exception) {
                        row {
                            label("创建配置项 ${configItem.key} 时出错: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    /**
     * 添加键值对字段
     */
    private fun Panel.addKeyValueField(config: KeyValueConfig) {
        val row = row(config.label) {
            val textField = when (config.inputType) {
                InputType.PASSWORD -> JBTextField(config.defaultValue ?: "").apply {
                    // 设置为密码字段
                }
                else -> JBTextField(config.defaultValue ?: "")
            }.also {
                components[config.key] = it
            }

            cell(textField)
                .align(Align.FILL)
                .applyToComponent {
                    if (config.required) {
                        emptyText.text = "此项为必填"
                    }
                }
        }

        config.description?.let {
            row.comment(it)
        }
    }

    /**
     * 添加下拉框字段
     */
    private fun Panel.addSelectField(config: SelectConfig) {
        val row = row(config.label) {
            val comboBox = ComboBox(config.options.map { it.label }.toTypedArray()).also {
                // 设置默认值
                if (config.defaultValue != null) {
                    val defaultOption = config.options.find { it.value == config.defaultValue }
                    if (defaultOption != null) {
                        it.selectedItem = defaultOption.label
                    }
                }
                components[config.key] = it
            }

            cell(comboBox)
                .align(Align.FILL)
        }

        config.description?.let {
            row.comment(it)
        }
    }

    /**
     * 添加复选框字段
     */
    private fun Panel.addCheckboxField(config: CheckboxConfig) {
        row {
            val checkBox = JBCheckBox(config.label, config.defaultValue).also {
                components[config.key] = it
            }

            cell(checkBox)
        }.comment(config.description ?: "")
    }

    /**
     * 添加列表字段
     */
    private fun Panel.addListField(config: ListConfig) {
        row(config.label) {
            // 这里可以实现一个支持添加/删除项的列表组件
            label("列表配置: ${config.key}")
                .also {
                    components[config.key] = it.component
                }
        }.comment(config.description ?: "列表配置项")
    }

    /**
     * 添加表格字段
     */
    private fun Panel.addTableField(config: TableConfig) {
        row(config.label) {
            // 这里可以实现一个表格组件
            label("表格配置: ${config.key}")
                .also {
                    components[config.key] = it.component
                }
        }.comment(config.description ?: "表格配置项")
    }

    /**
     * 添加条件字段
     */
    private fun Panel.addConditionalField(config: ConditionalConfig) {
        // 简化实现 - 直接添加内部配置项
        // 实际实现中应该根据条件动态显示/隐藏
        val row = row(config.label) {
            // 根据内部配置项类型添加对应组件
            when (val innerConfig = config.configItem) {
                is KeyValueConfig -> {
                    val textField = JBTextField(innerConfig.defaultValue ?: "").also {
                        components[config.key] = it
                    }
                    cell(textField).align(Align.FILL)
                }
                is SelectConfig -> {
                    val comboBox = ComboBox(innerConfig.options.map { it.label }.toTypedArray()).also {
                        components[config.key] = it
                    }
                    cell(comboBox).align(Align.FILL)
                }
                is CheckboxConfig -> {
                    val checkBox = JBCheckBox(innerConfig.label, innerConfig.defaultValue).also {
                        components[config.key] = it
                    }
                    cell(checkBox)
                }
                else -> {
                    label("条件配置: ${config.key}").also {
                        components[config.key] = it.component
                    }
                }
            }
        }

        config.description?.let {
            row.comment(it)
        }
    }

    /**
     * 验证表单数据
     *
     * @param configItems 配置项列表
     * @return 验证结果列表
     */
    fun validateFormData(configItems: List<ConfigItem>): List<ValidationInfo> {
        val validationInfos = mutableListOf<ValidationInfo>()

        configItems.filterIsInstance<KeyValueConfig>()
            .filter { it.required }
            .forEach { config ->
                val component = components[config.key]
                if (component is JBTextField) {
                    if (component.text.isNullOrBlank()) {
                        validationInfos.add(ValidationInfo("${config.label} 不能为空", component))
                    }
                }
            }

        return validationInfos
    }

    /**
     * 获取表单数据
     *
     * @return 表单数据键值对映射
     */
    fun getFormData(): Map<String, Any?> {
        val data = mutableMapOf<String, Any?>()
        components.forEach { (key, component) ->
            when (component) {
                is JBTextField -> {
                    data[key] = component.text
                }
                is ComboBox<*> -> {
                    // 获取选中的值
                    val selectedIndex = component.selectedIndex
                    if (selectedIndex >= 0) {
                        val configItem = configItemsMap[key]
                        if (configItem is SelectConfig) {
                            val selectedOption = configItem.options.getOrNull(selectedIndex)
                            data[key] = selectedOption?.value
                        }
                    }
                }
                is JBCheckBox -> {
                    data[key] = component.isSelected
                }
                else -> {
                    // 其他类型的组件处理
                    data[key] = null
                }
            }
        }
        return data
    }

    /**
     * 设置表单数据
     *
     * @param data 表单数据键值对映射
     */
    fun setFormData(data: Map<String, Any?>) {
        originalData.clear()
        originalData.putAll(data)

        data.forEach { (key, value) ->
            val component = components[key]
            when (component) {
                is JBTextField -> {
                    component.text = value?.toString() ?: ""
                }
                is ComboBox<*> -> {
                    if (value != null) {
                        val configItem = configItemsMap[key]
                        if (configItem is SelectConfig) {
                            val optionIndex = configItem.options.indexOfFirst { it.value == value.toString() }
                            if (optionIndex >= 0) {
                                component.selectedIndex = optionIndex
                            }
                        }
                    }
                }
                is JBCheckBox -> {
                    component.isSelected = value?.toString()?.toBoolean() ?: false
                }
            }
        }
    }

    /**
     * 从配置实例设置表单数据
     *
     * @param configInstance 配置实例
     */
    fun setFormDataFromConfig(configInstance: Any) {
        val configClass = configInstance::class
        val data = mutableMapOf<String, Any?>()

        // 遍历配置类的所有属性并获取值
        configClass.memberProperties.forEach { property ->
            try {
                val value = property.getter.call(configInstance)
                val key = "${configClass.simpleName}.${property.name}"
                data[key] = value
            } catch (e: IllegalAccessException) {
                // 无法访问属性
            } catch (e: InvocationTargetException) {
                // 调用getter方法时出错
            }
        }

        setFormData(data)
    }

    /**
     * 检查表单是否有修改
     *
     * @return 如果表单有修改则返回true，否则返回false
     */
    fun isModified(): Boolean {
        val currentData = getFormData()
        return originalData != currentData
    }
}

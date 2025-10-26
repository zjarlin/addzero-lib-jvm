package site.addzero.ide.config.model

/**
 * 配置项数据类接口
 */
interface ConfigItem {
    val key: String
    val label: String
    val description: String?
    val required: Boolean
}

/**
 * 基础键值对配置项
 *
 * @param key 配置项的键
 * @param label 显示标签
 * @param description 配置项描述
 * @param defaultValue 默认值
 * @param required 是否必填
 */
data class KeyValueConfig(
    override val key: String,
    override val label: String,
    override val description: String? = null,
    val defaultValue: String? = null,
    override val required: Boolean = false,
    val inputType: InputType = InputType.TEXT
) : ConfigItem

/**
 * 下拉框配置项
 *
 * @param key 配置项的键
 * @param label 显示标签
 * @param description 配置项描述
 * @param options 下拉选项
 * @param defaultValue 默认值
 * @param required 是否必填
 */
data class SelectConfig(
    override val key: String,
    override val label: String,
    override val description: String? = null,
    val options: List<SelectOption>,
    val defaultValue: String? = null,
    override val required: Boolean = false
) : ConfigItem

/**
 * 复选框配置项
 *
 * @param key 配置项的键
 * @param label 显示标签
 * @param description 配置项描述
 * @param defaultValue 默认值
 */
data class CheckboxConfig(
    override val key: String,
    override val label: String,
    override val description: String? = null,
    val defaultValue: Boolean = false,
    override val required: Boolean = false
) : ConfigItem

/**
 * 下拉选项
 *
 * @param value 选项值
 * @param label 选项标签
 */
data class SelectOption(
    val value: String,
    val label: String
)

/**
 * 列表配置项，支持二维配置
 *
 * @param key 配置项的键
 * @param label 显示标签
 * @param description 配置项描述
 * @param itemTemplate 列表项模板
 * @param maxItems 最大项数限制
 * @param minItems 最小项数限制
 */
data class ListConfig(
    override val key: String,
    override val label: String,
    override val description: String? = null,
    val itemTemplate: List<ConfigItem>,
    val maxItems: Int? = null,
    val minItems: Int? = null,
    override val required: Boolean = false
) : ConfigItem

/**
 * 表格配置项，用于二维列表配置
 *
 * @param key 配置项的键
 * @param label 显示标签
 * @param description 配置项描述
 * @param columns 表格列定义
 * @param maxRows 最大行数限制
 * @param minRows 最小行数限制
 */
data class TableConfig(
    override val key: String,
    override val label: String,
    override val description: String? = null,
    val columns: List<ConfigItem>,
    val maxRows: Int? = null,
    val minRows: Int? = null,
    override val required: Boolean = false
) : ConfigItem

/**
 * 条件配置项，用于支持配置项之间的依赖关系
 *
 * @param key 配置项的键
 * @param label 显示标签
 * @param description 配置项描述
 * @param condition 显示条件
 * @param configItem 实际的配置项
 */
data class ConditionalConfig(
    override val key: String,
    override val label: String,
    override val description: String? = null,
    val condition: Condition,
    val configItem: ConfigItem,
    override val required: Boolean = false
) : ConfigItem

/**
 * 条件表达式
 *
 * @param field 依赖的字段
 * @param operator 操作符
 * @param value 比较值
 */
data class Condition(
    val field: String,
    val operator: ConditionOperator,
    val value: String
)

/**
 * 条件操作符枚举
 */
enum class ConditionOperator {
    EQUALS,
    NOT_EQUALS,
    CONTAINS,
    NOT_CONTAINS,
    GREATER_THAN,
    LESS_THAN
}

/**
 * 输入类型枚举
 */
enum class InputType {
    TEXT,
    PASSWORD,
    NUMBER,
    EMAIL,
    URL
}
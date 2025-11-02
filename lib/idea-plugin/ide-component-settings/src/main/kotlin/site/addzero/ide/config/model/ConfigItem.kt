package site.addzero.ide.config.model

/**
 * 配置项数据类
 * 代表一个具体的配置项
 */
data class ConfigItem(
    val key: String,
    val label: String,
    val description: String,
    val required: Boolean,
    val inputType: InputType,
    val options: List<SelectOption> = emptyList()
)

/**
 * 下拉选项数据类
 */
data class SelectOption(
    val value: String,
    val label: String
)

/**
 * 配置字段的输入类型枚举
 */
enum class InputType {
    TEXT,       // 文本输入
    NUMBER,     // 数字输入
    PASSWORD,   // 密码输入
    TEXTAREA,   // 多行文本输入
    CHECKBOX,   // 复选框
    SELECT      // 下拉选择
}
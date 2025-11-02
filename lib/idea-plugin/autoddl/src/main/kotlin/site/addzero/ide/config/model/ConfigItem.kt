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

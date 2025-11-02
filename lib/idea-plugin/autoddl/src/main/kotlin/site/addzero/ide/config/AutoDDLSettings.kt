package site.addzero.ide.config

import site.addzero.ide.config.annotation.*
import site.addzero.ide.config.model.InputType

/**
 * AutoDDL 插件设置配置类
 */
@SettingRoute("AutoDDL")
@Configurable
data class AutoDDLSettings(
    @ConfigField(
        label = "数据库连接URL",
        description = "数据库连接字符串",
        required = true
    )
    val databaseUrl: String = "jdbc:mysql://localhost:3306/mydb",

    @ConfigField(
        label = "用户名",
        description = "数据库用户名",
        required = true
    )
    val username: String = "root",

    @ConfigField(
        label = "密码",
        description = "数据库密码",
        inputType = InputType.PASSWORD
    )
    val password: String = "",

    @ConfigSelect(
        label = "数据库类型",
        description = "选择数据库类型",
        optionsValue = ["mysql", "postgresql", "oracle"],
        optionsLabel = ["MySQL", "PostgreSQL", "Oracle"]
    )
    val databaseType: String = "mysql",

    @ConfigCheckbox(
        label = "启用SSL",
        description = "是否使用SSL连接数据库"
    )
    val enableSsl: Boolean = false,

    @ConfigField(
        label = "连接超时时间",
        description = "数据库连接超时时间（毫秒）",
        inputType = InputType.NUMBER
    )
    val connectionTimeout: Int = 5000
)
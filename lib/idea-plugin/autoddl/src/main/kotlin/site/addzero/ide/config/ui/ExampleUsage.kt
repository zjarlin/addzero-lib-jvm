package site.addzero.ide.config.ui

import site.addzero.ide.config.annotation.*
import site.addzero.ide.config.model.InputType
import site.addzero.ide.config.registry.ConfigRegistry
import javax.swing.JPanel

/**
 * 通用设置模块使用示例
 *
 * 本文件展示了如何使用 BaseConfigurableTreeUI 创建自定义的设置面板
 */

// ==================== 示例 1: 基本使用 ====================

/**
 * 示例 1: 使用默认扫描器创建设置面板
 */
class BasicExampleUI : BaseConfigurableTreeUI(
    displayName = "基础设置示例"
) {
    // 使用默认的 ConfigScanner.scanAndRegisterConfigs()
}

// ==================== 示例 2: 自定义扫描器 ====================

/**
 * 示例 2: 自定义配置扫描逻辑
 */
class CustomScannerExampleUI : BaseConfigurableTreeUI(
    displayName = "自定义扫描器示例",
    configScanner = {
        // 自定义扫描逻辑
        ConfigRegistry.registerConfig(ExampleConfig1::class)
        ConfigRegistry.registerConfig(ExampleConfig2::class)
        // 可以添加更多自定义逻辑
    }
)

// ==================== 示例 3: 完整自定义 ====================

/**
 * 示例 3: 继承并重写方法以实现完全自定义的行为
 */
class AdvancedExampleUI(
    private val myDisplayName: String = "高级设置示例"
) : BaseConfigurableTreeUI(myDisplayName) {

    override fun createTreePanel(): JPanel {
        val panel = super.createTreePanel()
        // 可以在这里添加自定义逻辑
        return panel
    }

    override fun createConfigPanel(
        configInfo: site.addzero.ide.config.registry.ConfigRouteInfo,
        formBuilder: site.addzero.ide.ui.form.DynamicFormBuilder
    ): JPanel {
        val panel = super.createConfigPanel(configInfo, formBuilder)
        // 可以在这里自定义配置面板
        return panel
    }
}

// ==================== 示例配置类 ====================

/**
 * 示例配置类 1: 应用配置
 */
@SettingRoute("示例设置")
@Configurable
data class ExampleConfig1(
    @ConfigField(
        label = "应用名称",
        description = "应用的显示名称",
        required = true
    )
    val appName: String = "我的应用",

    @ConfigField(
        label = "版本",
        description = "应用版本号"
    )
    val version: String = "1.0.0",

    @ConfigCheckbox(
        label = "启用调试",
        description = "是否启用调试模式"
    )
    val debugEnabled: Boolean = false,

    @ConfigSelect(
        label = "语言",
        description = "应用界面语言",
        options = [
            SelectOption("zh-CN", "中文"),
            SelectOption("en-US", "English"),
            SelectOption("ja-JP", "日本語")
        ]
    )
    val language: String = "zh-CN"
)

/**
 * 示例配置类 2: 网络配置
 */
@SettingRoute("示例设置")
@Configurable
data class ExampleConfig2(
    @ConfigField(
        label = "代理服务器",
        description = "代理服务器地址"
    )
    val proxyHost: String = "",

    @ConfigField(
        label = "代理端口",
        description = "代理服务器端口",
        inputType = InputType.NUMBER
    )
    val proxyPort: Int = 8080,

    @ConfigCheckbox(
        label = "使用代理",
        description = "是否使用代理服务器"
    )
    val useProxy: Boolean = false,

    @ConfigField(
        label = "超时时间",
        description = "网络请求超时时间（毫秒）",
        inputType = InputType.NUMBER
    )
    val timeout: Int = 5000
)

// ==================== 在插件中使用 ====================

/**
 * 在 plugin.xml 中注册设置面板的示例:
 *
 * <applicationConfigurable
 *     parentId="tools"
 *     instance="site.addzero.ide.config.ui.CustomScannerExampleUI"
 *     id="example.settings">
 *     <label>示例设置</label>
 * </applicationConfigurable>
 */

/**
 * 使用步骤:
 *
 * 1. 定义配置类，使用 @SettingRoute 和 @Configurable 注解
 * 2. 创建 BaseConfigurableTreeUI 实例或继承它
 * 3. 在 plugin.xml 中注册为 Configurable
 *
 * 示例代码:
 *
 * ```kotlin
 * class MyPluginSettings : BaseConfigurableTreeUI(
 *     displayName = "我的插件设置"
 * )
 * ```
 *
 * XML配置:
 * ```xml
 * <applicationConfigurable
 *     parentId="tools"
 *     instance="my.package.MyPluginSettings"
 *     id="my.plugin.settings">
 *     <label>我的插件设置</label>
 * </applicationConfigurable>
 * ```
 */

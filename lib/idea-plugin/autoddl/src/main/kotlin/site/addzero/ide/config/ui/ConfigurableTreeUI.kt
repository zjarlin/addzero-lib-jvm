package site.addzero.ide.config.ui

import site.addzero.ide.config.scanner.ConfigScanner

/**
 * AutoDDL 树形配置界面
 * 使用 BaseConfigurableTreeUI 基类实现
 */
class ConfigurableTreeUI(
    displayName: String = "AutoDDL 配置",
    configScanner: () -> Unit = { ConfigScanner.scanAndRegisterConfigs() }
) : BaseConfigurableTreeUI(displayName, configScanner)
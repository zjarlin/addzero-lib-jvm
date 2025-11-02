package site.addzero.ide.config.ui

import site.addzero.ide.config.AutoDDLSettings
import site.addzero.ide.config.annotation.SettingRoute
import site.addzero.ide.config.registry.ConfigRegistry

/**
 * AutoDDL 插件配置界面
 */
class AutoDDLConfigurable : BaseConfigurableTreeUI(
    displayName = "AutoDDL Settings",
    configScanner = {
        // 注册 AutoDDL 设置配置类
        ConfigRegistry.registerConfig(AutoDDLSettings::class)
    }
)
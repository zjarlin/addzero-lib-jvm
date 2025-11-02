package site.addzero.ide.config.ui

import site.addzero.ide.config.AutoDDLSettings
import site.addzero.ide.config.registry.ConfigRegistry

/**
 * AutoDDL 插件项目级别配置界面
 */
class AutoDDLProjectConfigurable : BaseConfigurableTreeUI(
    labelName = "AutoDDL",
    configScanner = {
        // 注册 AutoDDL 设置配置类
        ConfigRegistry.registerConfig(AutoDDLSettings::class)
    }
)
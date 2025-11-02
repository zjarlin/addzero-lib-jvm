package site.addzero.ide.config.ui

import site.addzero.ide.config.AutoDDLSettings
import site.addzero.ide.config.registry.ConfigRegistry

/**
 * AutoDDL 插件项目级别配置界面
 */
class AutoDDLProjectConfigurable : BaseConfigurableTreeUI(
    labelName = "AutoDDL",
    project = null, // 暂时设置为null，我们需要在运行时获取Project
    configScanner = {
        // 注册 AutoDDL 设置配置类
        ConfigRegistry.registerConfig(AutoDDLSettings::class)
    }
)
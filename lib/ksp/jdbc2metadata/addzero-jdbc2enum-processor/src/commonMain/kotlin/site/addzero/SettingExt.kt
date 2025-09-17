package site.addzero

import site.addzero.context.SettingContext.settings
import site.addzero.context.Settings
import site.addzero.util.str.withPkg

/**
     * 枚举输出目录（shared 编译目录）
     */
    val Settings.enumOutputDir: String
        get() = settings.sharedSourceDir.withPkg(settings.enumOutputPackage)

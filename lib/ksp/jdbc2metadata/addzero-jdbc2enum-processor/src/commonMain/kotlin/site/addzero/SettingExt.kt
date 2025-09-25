package site.addzero

import site.addzero.context.SettingContext.settings
import site.addzero.context.Settings
import site.addzero.util.str.withPkg

/**
 * 枚举输出目录（shared 编译目录）
 */
val Settings.enumOutputDir: String
    get() = run {
        val sharedSourceDir: String = settings.sharedSourceDir
        if (sharedSourceDir.isBlank()) {
            error("您没有设置要生成的目标共享目录位置")
        }
        sharedSourceDir.withPkg(settings.enumOutputPackage)
    }

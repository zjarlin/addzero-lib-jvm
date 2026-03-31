package site.addzero

import site.addzero.jdbc2enum.processor.context.Settings
import site.addzero.util.str.withPkg

/**
 * 枚举输出目录（shared 编译目录）
 */
val Settings.enumOutputDir: String
    get() = run {
        val targetSharedSourceDir = this.sharedSourceDir
        if (targetSharedSourceDir.isBlank()) {
            error("您没有设置要生成的目标共享目录位置")
        }
        targetSharedSourceDir.withPkg(this.enumOutputPackage)
    }

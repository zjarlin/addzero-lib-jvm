package site.addzero.common.kt_util

import cn.hutool.core.io.FileUtil
import cn.hutool.system.SystemUtil
import site.addzero.util.str.containsAnyIgnoreCase


object OsUtil {

    fun getAppDataDir(appName: String): String {
        return if (SystemUtil.getOsInfo().isWindows) {
            // Windows 应用数据目录：`%APPDATA%\appName`
            FileUtil.normalize("${SystemUtil.get("APPDATA")}/$appName")
        } else if (SystemUtil.getOsInfo().isMac) {
            // macOS 应用数据目录：`~/Library/Application Support/appName`
            FileUtil.normalize("${FileUtil.getUserHomePath()}/Library/Application Support/$appName")
        } else {
            // Linux/Unix 应用数据目录：`~/.config/appName`
            FileUtil.normalize("${FileUtil.getUserHomePath()}/.config/$appName")
        }
    }

    /**
     * 获取当前平台类型
     */
    fun getPlatformType(): PlatformType {
        val osInfo = SystemUtil.getOsInfo()
        return when {
            osInfo.isWindows -> PlatformType.WINDOWS
            osInfo.isMac -> PlatformType.MAC
            osInfo.name.containsAnyIgnoreCase("nix", "nux", "aix") -> PlatformType.LINUX
            else -> PlatformType.UNKNOWN
        }
    }


    fun openFolder(path: String) {
        val quotedPath = CmdUtil.quoteArg(path)
        when (getPlatformType()) {
            PlatformType.WINDOWS, PlatformType.UNKNOWN -> {
                CmdUtil.runCmd("explorer.exe $quotedPath")
            }

            PlatformType.MAC -> {
                CmdUtil.runCmd("open $quotedPath")
            }

            PlatformType.LINUX -> {
                CmdUtil.runCmd("xdg-open $quotedPath")
            }
        }
    }
}

enum class PlatformType {
    UNKNOWN, WINDOWS, MAC, LINUX,
}

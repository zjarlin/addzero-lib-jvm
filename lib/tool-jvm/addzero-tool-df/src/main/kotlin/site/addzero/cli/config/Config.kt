package site.addzero.cli.config

import kotlinx.serialization.Serializable
import site.addzero.cli.platform.PlatformService
import site.addzero.cli.platform.PlatformType.*
import site.addzero.cli.setting.SettingContext
import site.addzero.cli.setting.SettingContext.DEFAULT_PKG

/**
 * 配置文件类
 */
@Serializable
data class Config(
    /** 同步 目录路径，默认为XDG目录(规范目录)下的 .dotfiles 文件夹 */
    val syncDir: String = SettingContext.SYNC_DIR,
    /** 云同步类型 */
    val syncType: SyncType = SyncType.GIT,
    /** 云同步地址("mac") */
    val cloudUrl: String? = null,

    /** Linux系统特定配置 */
    private val linuxConfig: PlatformConfig = PlatformConfig(
        defaultPackages = DEFAULT_PKG
    ),

    /** macOS系统特定配置 */
    private val macConfig: PlatformConfig = PlatformConfig(packageManager = "brew", defaultPackages = DEFAULT_PKG),

    /** Windows系统特定配置 */
    private val windowsConfig: PlatformConfig = PlatformConfig(packageManager = "winget"),
) {

    fun getCurrentPlatformConfig(): PlatformConfig {
        return when (PlatformService.getPlatformType()) {
            WINDOWS -> windowsConfig
            MACOS -> macConfig
            LINUX -> linuxConfig
            UNKNOWN -> linuxConfig
        }
    }
}

/**
 * 平台特定配置类
 */
@Serializable
data class PlatformConfig(
    /** 平台特定的包管理器 */
    val packageManager: String? = null,

    /** 平台特定的默认软件包列表 */
    val defaultPackages: Set<String> = emptySet(),

    /** 平台特定的PATH目录 */
    val links: Set<Lines> = emptySet(),

    /** 离线环境内置软件安装记录 */
    val nativatePkgRecord: Set<NativePkgRecordType> = emptySet()
)

enum class NativePkgRecordType {
    // TODO:    以后再写内置软件内网环境的安装逻辑
    JDK17, JDK25, TEAMCITY, NODE, NPM, YARN, GIT, CURL, WGET, ZSH, NVIM, NE
}

@Serializable
data class Lines(
    val source: String,
    val target: String
)

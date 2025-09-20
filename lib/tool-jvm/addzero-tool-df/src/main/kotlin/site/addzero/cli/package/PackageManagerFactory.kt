package site.addzero.cli.`package`

import kotlinx.coroutines.runBlocking
import site.addzero.cli.platform.PlatformService
import site.addzero.cli.platform.PlatformType

/**
 * 包管理器工厂类
 *
 * 用于创建对应平台的包管理器实现
 */
object PackageManagerFactory {
    /**
     * 获取默认包管理器
     *
     * @return 包管理器实现
     */
    fun getDefaultPackageManager(): PackageManager {
        val osType = PlatformService.getPlatformType()

        return when (osType) {
            PlatformType.WINDOWS -> {
                // 优先使用Winget，如果不可用则使用Chocolatey
                val winget = WingetPackageManager()
                if (runBlockingSafe { winget.isAvailable() }) {
                    winget
                } else {
                    ChocolateyPackageManager()
                }
            }

            PlatformType.MACOS -> HomebrewPackageManager()
            PlatformType.LINUX -> AptPackageManager()
            else -> throw UnsupportedOperationException("不支持的操作系统")
        }
    }

    /**
     * 根据名称获取包管理器
     *
     * @param name 包管理器名称
     * @return 包管理器实现，如果不存在则返回null
     */
    fun getPackageManager(name: String): PackageManager? {
        return when (name.lowercase()) {
            "apt", "apt-get" -> AptPackageManager()
            "brew", "homebrew" -> HomebrewPackageManager()
            "choco", "chocolatey" -> ChocolateyPackageManager()
            "winget" -> WingetPackageManager()
            else -> null
        }
    }

    /**
     * 获取所有支持的包管理器名称
     *
     * @return 包管理器名称列表
     */
    fun getAllPackageManagerNames(): List<String> {
        return listOf("apt", "brew", "choco", "winget")
    }

    /**
     * 获取当前平台所有可用的包管理器
     *
     * @return 可用的包管理器列表
     */
    fun getAllSupportedPackageManagers(): List<PackageManager> {
        val packageManagers = mutableListOf<PackageManager>()

        // 检查APT
        val apt = AptPackageManager()
        if (runBlockingSafe { apt.isAvailable() }) {
            packageManagers.add(apt)
        }

        // 检查Homebrew
        val brew = HomebrewPackageManager()
        if (runBlockingSafe { brew.isAvailable() }) {
            packageManagers.add(brew)
        }

        // 检查Chocolatey
        val choco = ChocolateyPackageManager()
        if (runBlockingSafe { choco.isAvailable() }) {
            packageManagers.add(choco)
        }

        // 检查Winget
        val winget = WingetPackageManager()
        if (runBlockingSafe { winget.isAvailable() }) {
            packageManagers.add(winget)
        }

        return packageManagers
    }

    /**
     * 安全地运行挂起函数
     *
     * @param block 挂起函数块
     * @return 函数执行结果
     */
    private fun <T> runBlockingSafe(block: suspend () -> T): T {
        return runBlocking {
            block()
        }
    }
}

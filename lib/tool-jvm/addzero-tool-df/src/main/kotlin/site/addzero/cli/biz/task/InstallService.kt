package site.addzero.cli.biz.task

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import site.addzero.cli.config.ConfigService
import site.addzero.cli.`package`.PackageManagerFactory
import site.addzero.cli.platform.PlatformService

/**
 * 安装任务
 *
 * 负责安装指定的软件包列表
 */
@Single
class InstallService(
    private val configService: ConfigService
) : AbsTask {
    val config = configService.config
    val currentPlatformConfig = config.getCurrentPlatformConfig()

    val packages = currentPlatformConfig.defaultPackages

    override val des: String = "安装指定的软件包列表"

    override suspend fun executeInternal(scope: CoroutineScope) = withContext(Dispatchers.Default) {
        println("安装软件包...")
        // 如果没有指定软件包，则使用配置文件中的默认软件包
        val packagesToInstall = packages.ifEmpty {

            if (packages.isEmpty()) {
                println("没有指定要安装的软件包，且配置文件中没有默认软件包,请先设置")
                configService.addPkg(PlatformService.readLine() ?: "")
                return@withContext
            }
            println("使用配置文件中的默认软件包: ${packages.joinToString(", ")}")
            packages
        }

        // 获取包管理器，优先使用配置文件中指定的包管理器
        val packageManager = run {
            val platformPackageManager = currentPlatformConfig.packageManager

            if (!platformPackageManager.isNullOrEmpty()) {
                val pm = PackageManagerFactory.getPackageManager(platformPackageManager)
                if (pm != null) {
                    println("使用平台特定的包管理器: ${pm.getName()}")
                    pm
                } else {
                    println("平台特定的包管理器 $platformPackageManager 不可用，尝试使用全局配置")
                    // 注意：Config类中没有全局packageManager属性，直接使用默认包管理器
                    PackageManagerFactory.getDefaultPackageManager()
                }
            } else {
                PackageManagerFactory.getDefaultPackageManager()
            }
        }

        println("使用包管理器: ${packageManager.getName()}")

        // 确保包管理器可用
        if (!packageManager.isAvailable()) {
            println("${packageManager.getName()} 不可用，尝试安装...")
            val installed = packageManager.installSelf()
            if (!installed) {
                throw RuntimeException("无法安装 ${packageManager.getName()}")
            }
        }

        // 更新索引
        println("更新包索引...")
        packageManager.updateIndex()

        // 安装软件包
        val successPackages = run {
            val concurrency = PlatformService.getAvailableProcessors()
            println("使用并发模式安装软件包，并发数: $concurrency")
            packageManager.installPackagesConcurrently(packagesToInstall.toList())
        }

        println("安装完成: ${successPackages.size}/${packagesToInstall.size} 个软件包成功安装")
        if (successPackages.size < packagesToInstall.size) {
            val failedPackages = packagesToInstall.filter { it !in successPackages }
            println("以下软件包安装失败: ${failedPackages.joinToString(", ")}")
        }
    }

}

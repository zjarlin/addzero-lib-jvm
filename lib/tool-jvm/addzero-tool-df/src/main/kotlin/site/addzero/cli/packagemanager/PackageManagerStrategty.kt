package site.addzero.cli.packagemanager

import org.koin.core.annotation.Single
import org.koin.java.KoinJavaComponent
import site.addzero.cli.platform.PlatformService

@Single
class TempContainer(val strategys: List<PackageManagerStrategty>)

val temp by KoinJavaComponent.inject<TempContainer>(TempContainer::class.java)

/**
 * 包管理器抽象接口
 *
 * 提供与包管理器相关的功能抽象，如安装、卸载、更新软件包等
 */
interface PackageManagerStrategty {
    companion object Companion {
        public fun getSupportPackageManager(): PackageManagerStrategty {
            val listT = temp.strategys
            val firstOrNull = listT.firstOrNull { it.support }
            val supportStrategty = firstOrNull
            return supportStrategty ?: throw Exception("No support package manager")
        }
    }

    val support: Boolean

    /**
     * 获取包管理器名称
     */
    fun getName(): String

    /**
     * 检查包管理器是否可用
     *
     * @return 是否可用
     */
    suspend fun isAvailable(): Boolean

    /**
     * 安装包管理器（如果不存在）
     *
     * @return 是否安装成功
     */
    suspend fun installSelf(): Boolean

    /**
     * 更新包管理器索引
     *
     * @return 是否更新成功
     */
    suspend fun updateIndex(): Boolean

    /**
     * 安装软件包
     *
     * @param packageName 软件包名称
     * @return 是否安装成功
     */
    suspend fun installPackage(packageName: String): Boolean

    /**
     * 批量安装软件包（串行）
     *
     * @param packageNames 软件包名称列表
     * @return 安装成功的软件包列表
     */
    suspend fun installPackages(packageNames: List<String>): List<String>

    /**
     * 并发批量安装软件包
     *
     * @param packageNames 软件包名称列表
     * @param concurrency 并发数，默认为系统可用处理器数量
     * @return 安装成功的软件包列表
     */
    suspend fun installPackagesConcurrently(
        packageNames: List<String>,
        concurrency: Int = PlatformService.getAvailableProcessors()
    ): List<String>

    /**
     * 卸载软件包
     *
     * @param packageName 软件包名称
     * @return 是否卸载成功
     */
    suspend fun uninstallPackage(packageName: String): Boolean

    /**
     * 检查软件包是否已安装
     *
     * @param packageName 软件包名称
     * @return 是否已安装
     */
    suspend fun isPackageInstalled(packageName: String): Boolean

    /**
     * 获取软件包版本
     *
     * @param packageName 软件包名称
     * @return 软件包版本，如果不存在则返回null
     */
    suspend fun getPackageVersion(packageName: String): String?

    /**
     * 搜索软件包
     *
     * @param keyword 关键字
     * @return 匹配的软件包列表
     */
    suspend fun searchPackage(keyword: String): List<String>
}

/**
 * 包管理器安装结果
 *
 * @property success 是否成功
 * @property message 消息
 */
data class PackageInstallResult(
    val success: Boolean,
    val message: String
)

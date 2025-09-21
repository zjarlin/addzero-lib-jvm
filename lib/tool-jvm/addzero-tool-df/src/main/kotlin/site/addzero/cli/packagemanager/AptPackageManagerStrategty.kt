package site.addzero.cli.packagemanager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single
import site.addzero.cli.platform.PlatformService
import site.addzero.cli.platform.PlatformType
import site.addzero.cli.platform.runBoolean
import site.addzero.cli.platform.runCmd

/**
 * Linux的APT包管理器实现
 */
@Single
class AptPackageManagerStrategty : PackageManagerStrategty {
    override val support: Boolean
        get() {
            val osType = PlatformService.getPlatformType()
            val bool = osType == PlatformType.LINUX
            val runBoolean = "which apt".runBoolean()
            return bool && runBoolean
        }

    override fun getName(): String = "APT"

    override suspend fun isAvailable(): Boolean {
        return "which opt".runBoolean()
    }

    override suspend fun installSelf(): Boolean {
        val available = isAvailable()
        if (available) {
            println("已安装过包管理器apt,跳过")
        }
        return available
    }

    override suspend fun updateIndex(): Boolean {
        return "sudo apt update".runBoolean()
    }

    override suspend fun installPackage(packageName: String): Boolean {
        return "sudo apt install -y $packageName".runBoolean()
    }

    override suspend fun installPackages(packageNames: List<String>): List<String> {
        // APT支持一次安装多个包
        val packageList = packageNames.joinToString(" ")

        val runBoolean = "sudo apt install -y $packageList".runBoolean()
        return if (runBoolean) {
            packageNames
        } else {
            // 如果批量安装失败，尝试逐个安装
            val successPackages = mutableListOf<String>()
            for (packageName in packageNames) {
                if (installPackage(packageName)) {
                    successPackages.add(packageName)
                }
            }
            successPackages
        }
    }

    override suspend fun installPackagesConcurrently(packageNames: List<String>, concurrency: Int): List<String> =
        coroutineScope {
            // 将包列表分成多个批次，每个批次的大小不超过并发数
            val batches = packageNames.chunked(concurrency)
            val allSuccessPackages = mutableListOf<String>()

            for (batch in batches) {
                val deferreds = batch.map { packageName ->
                    async(Dispatchers.Default) {
                        val success = installPackage(packageName)
                        if (success) packageName else null
                    }
                }

                val results = deferreds.awaitAll()
                allSuccessPackages.addAll(results.filterNotNull())
            }

            return@coroutineScope allSuccessPackages
        }

    override suspend fun uninstallPackage(packageName: String): Boolean {
        return "sudo apt remove -y $packageName".runBoolean()
    }

    override suspend fun isPackageInstalled(packageName: String): Boolean {
        return "dpkg -l | grep -w $packageName".runBoolean()
    }

    override suspend fun getPackageVersion(packageName: String): String? {
        if (!isPackageInstalled(packageName)) {
            return null
        }

        val result = "dpkg -s $packageName | grep Version | cut -d ' ' -f 2".runCmd()

        return if (result.exitCode == 0 && result.output.isNotBlank()) result.output.trim() else null
    }

    override suspend fun searchPackage(keyword: String): List<String> {
        val result = "apt search $keyword".runCmd()


        return if (result.exitCode == 0) {
            // 解析输出获取包名列表
            val lines = result.output.split("\n").filter { it.isNotBlank() && it.contains("/") }
            val packages = mutableListOf<String>()

            for (line in lines) {
                val regex = "^([\\w.-]+)/".toRegex()
                val matchResult = regex.find(line)
                matchResult?.groupValues?.getOrNull(1)?.let {
                    packages.add(it)
                }
            }

            packages
        } else {
            emptyList()
        }
    }
}

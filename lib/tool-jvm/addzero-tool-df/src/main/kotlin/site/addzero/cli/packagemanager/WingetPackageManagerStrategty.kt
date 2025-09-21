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
 * Windows的Winget包管理器实现
 */
@Single
class WingetPackageManagerStrategty : PackageManagerStrategty {
    val osType = PlatformService.getPlatformType()
    override val support: Boolean
        get() {
            val osType = PlatformService.getPlatformType()
            return osType == PlatformType.WINDOWS
        }

    override fun getName(): String = "Winget"

    override suspend fun isAvailable(): Boolean {
        return "winget --version".runBoolean()
    }

    override suspend fun installSelf(): Boolean {
        if (isAvailable()) {
            println("已安装过包管理器winget,跳过")
            return true
        }
        println("Winget不可用，请确保您的Windows版本支持Winget或从Microsoft Store安装")
        return false
    }

    override suspend fun updateIndex(): Boolean {
        // Winget会自动更新源，无需手动更新索引
        return true
    }

    override suspend fun installPackage(packageName: String): Boolean {
        return "winget install --silent --accept-package-agreements --accept-source-agreements $packageName".runBoolean()
    }

    override suspend fun installPackages(packageNames: List<String>): List<String> {
        val successPackages = mutableListOf<String>()

        for (packageName in packageNames) {
            if (installPackage(packageName)) {
                successPackages.add(packageName)
            }
        }

        return successPackages
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
        return "winget uninstall --silent $packageName".runBoolean()
    }

    override suspend fun isPackageInstalled(packageName: String): Boolean {
        val result = "winget list --exact --query $packageName".runCmd()
        return result.exitCode == 0 && result.output.contains(packageName, ignoreCase = true)
    }

    override suspend fun getPackageVersion(packageName: String): String? {
        if (!isPackageInstalled(packageName)) {
            return null
        }

        val result = "winget show --exact --query $packageName".runCmd()



        if (result.exitCode == 0) {
            // 解析输出获取版本号
            val regex = "Version:\\s+([\\d.]+)".toRegex(RegexOption.IGNORE_CASE)
            val matchResult = regex.find(result.output)
            return matchResult?.groupValues?.getOrNull(1)
        }
        return null
    }

    override suspend fun searchPackage(keyword: String): List<String> {
        val result = "winget search $keyword".runCmd()

        if (result.exitCode == 0) {
            // 解析输出获取包名列表
            val lines = result.output.lines().drop(2) // 跳过标题行
            return lines.filter { it.isNotBlank() }.map { line ->
                line.split(Regex("\\s{2,}"))[0] // 获取第一列（包名）
            }
        }
        return emptyList()
    }
}


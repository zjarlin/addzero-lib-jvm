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
 * Windows的Chocolatey包管理器实现
 */
@Single

class ChocolateyPackageManagerStrategty : PackageManagerStrategty {
    override val support: Boolean
        get() {
            val osType = PlatformService.getPlatformType()
            val bool = osType == PlatformType.WINDOWS
            val runBoolean = "where choco".runBoolean()
            return runBoolean && bool
        }

    override fun getName(): String = "Chocolatey"

    override suspend fun isAvailable(): Boolean {
        return "where choco".runBoolean()
    }

    override suspend fun installSelf(): Boolean {
        if (isAvailable()) {
            println("已安装过包管理器Chocolate,跳过")
            return true
        }

        // 安装Chocolatey（需要管理员权限）
        val installScript =
            "Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))"
        return "powershell -Command \"$installScript\"".runBoolean()
    }

    override suspend fun updateIndex(): Boolean {
        // Chocolatey没有单独的更新索引命令，但可以通过升级自身来更新
        return "choco upgrade chocolatey -y".runBoolean()
    }

    override suspend fun installPackage(packageName: String): Boolean {
        return "choco install $packageName -y".runBoolean()
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
        return "choco uninstall $packageName -y".runBoolean()
    }

    override suspend fun isPackageInstalled(packageName: String): Boolean {
        return "choco list --local-only $packageName".runBoolean()
    }

    override suspend fun getPackageVersion(packageName: String): String? {
        if (!isPackageInstalled(packageName)) {
            return null
        }

        val result = "choco list --local-only $packageName".runCmd()


        if (result.exitCode == 0) {
            // 解析输出获取版本号
            val regex = "${packageName}\\s+([\\d.]+)".toRegex(RegexOption.IGNORE_CASE)
            val matchResult = regex.find(result.output)
            return matchResult?.groupValues?.getOrNull(1)
        }
        return null
    }

    override suspend fun searchPackage(keyword: String): List<String> {
        val result = "choco search $keyword".runCmd()


        return if (result.exitCode == 0) {
            // 解析输出获取包名列表
            val lines = result.output.split("\n").filter { it.isNotBlank() }
            val packages = mutableListOf<String>()

            for (line in lines) {
                val regex = "^([\\w.-]+)\\s+".toRegex()
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

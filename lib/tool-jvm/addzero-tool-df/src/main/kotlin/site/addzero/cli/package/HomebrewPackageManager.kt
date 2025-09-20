package site.addzero.cli.`package`

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import site.addzero.cli.platform.PlatformService
import site.addzero.cli.platform.runBoolean
import site.addzero.cli.platform.runCmd

/**
 * macOS的Homebrew包管理器实现
 */
class HomebrewPackageManager : PackageManager {
    val osType = PlatformService.getPlatformType()

    override fun getName(): String = "Homebrew"

    override suspend fun isAvailable(): Boolean {
        return "which brew".runBoolean()
    }

    override suspend fun installSelf(): Boolean {
        if (isAvailable()) {
            return true
        }
        // 安装Homebrew
        val installScript =
            """
        /bin/zsh -c "$(curl -fsSL https://gitee.com/cunkai/HomebrewCN/raw/master/Homebrew.sh)"
            """.trimIndent()
        return installScript.runBoolean()

    }

    override suspend fun updateIndex(): Boolean {
        return "brew update".runBoolean()
    }

    override suspend fun installPackage(packageName: String): Boolean {
        return "brew install $packageName".runBoolean()
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
        return "brew uninstall $packageName".runBoolean()
    }

    override suspend fun isPackageInstalled(packageName: String): Boolean {
        return "brew list --formula | grep -q '^$packageName\$'".runBoolean()
    }

    override suspend fun getPackageVersion(packageName: String): String? {
        if (!isPackageInstalled(packageName)) {
            return null
        }

        val result =
            "brew info --json=v1 $packageName | grep '\"installed\"' -A 3 | grep '\"version\"' | head -n 1 | awk -F\":\" '{print \$2}' | sed 's/[\",]//g' | tr -d '[[:space:]]'".runCmd()


        return if (result.exitCode == 0 && result.output.isNotBlank()) result.output.trim() else null
    }

    override suspend fun searchPackage(keyword: String): List<String> {
        val result = "brew search $keyword".runCmd()



        return if (result.exitCode == 0) {
            result.output.split("\n").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    }
}

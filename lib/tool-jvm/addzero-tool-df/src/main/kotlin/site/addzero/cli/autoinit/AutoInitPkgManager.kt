package site.addzero.cli.autoinit

import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Single
import site.addzero.cli.biz.task.InstallService
import site.addzero.cli.config.ConfigService
import site.addzero.cli.packagemanager.PackageManagerStrategty.Companion.getSupportPackageManager
import site.addzero.cli.platform.PlatformService
import site.addzero.util.inject


@Single(createdAtStart = true)
class AutoInitPkgManager {
    init {
        val configService = inject<ConfigService>()
        val supportPackageManager = getSupportPackageManager()
        val filterNot = configService.currentPlatformConfig.defaultPackages.filterNot {
            runBlocking {
                val packageInstalled = supportPackageManager.isPackageInstalled(it)
                packageInstalled
            }
        }
        val readBoolean =
            PlatformService.readBooleanDefaultNo("这些安装包还没有安装过$filterNot,是否确认安装?") ?: false
        if (readBoolean) {
            println("开始静默安装")
            //查看哪些包还没安装过

            val inject = inject<InstallService>()
            runBlocking {
                inject.executeInternal(this)
            }

        } else {
            println("您选择了不安装,继续执行其他任务")
        }


    }
}




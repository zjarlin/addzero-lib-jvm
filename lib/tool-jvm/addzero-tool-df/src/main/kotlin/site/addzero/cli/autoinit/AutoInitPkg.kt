package site.addzero.cli.autoinit

import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Single
import site.addzero.cli.packagemanager.PackageManagerStrategty


@Single(createdAtStart = true)
class AutoInitPkg {
    init {
        val packageManagerStrategty = PackageManagerStrategty.getSupportPackageManager()
        runBlocking {
            val installSelf = packageManagerStrategty.installSelf()
        }
    }

}

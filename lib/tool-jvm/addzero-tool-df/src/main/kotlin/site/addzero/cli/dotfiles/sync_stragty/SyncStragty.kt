package site.addzero.cli.dotfiles.sync_stragty

import org.koin.core.annotation.Single
import org.koin.java.KoinJavaComponent.inject
import site.addzero.cli.config.ConfigService
import site.addzero.cli.config.SyncType

private val configService by inject<ConfigService>(ConfigService::class.java)

@Single
class SyncTemp(val strategys: List<SyncStragty>)

val temp: SyncTemp by inject(SyncTemp::class.java)

val act = run {
    val config = configService.config
    // 根据配置中的同步类型选择合适的策略
    temp.strategys.firstOrNull { it.syncType == config.syncType } ?: GitSyncStrategy(configService)
}

object SyncUtil : SyncStragty by act {
    // 提供一个直接访问当前配置中同步类型的方法
    fun getCurrentSyncType(): SyncType {
        return configService.config.syncType
    }

    // 提供一个获取所有支持的同步类型的方法
    fun getSupportedSyncTypes(): List<SyncType> {
        return temp.strategys.map { it.syncType }
    }
}

interface SyncStragty {
    // 策略是否支持当前环境
    val support: Boolean get() = true

    // 执行同步操作
    fun pull(): Boolean

    fun commitAndPush(): Boolean

    // 同步类型
    val syncType: SyncType
}

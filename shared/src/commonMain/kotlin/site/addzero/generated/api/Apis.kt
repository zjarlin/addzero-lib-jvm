package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.mp.KoinPlatform
import site.addzero.kcloud.plugins.mcuconsole.api.external.*

/**
 * 聚合后的 Ktorfit 服务提供者
 *
 * 仅聚合 controller2api 生成的接口，不扫描手写接口。
 */
object Apis {
    private fun ktorfit(): Ktorfit = KoinPlatform.getKoin().get()

    /**
     * DeviceInfoApi 服务实例
     */
    val deviceInfoApi: DeviceInfoApi
        get() = ktorfit().createDeviceInfoApi()

    /**
     * FlashApi 服务实例
     */
    val flashApi: FlashApi
        get() = ktorfit().createFlashApi()

    /**
     * SerialPortApi 服务实例
     */
    val serialPortApi: SerialPortApi
        get() = ktorfit().createSerialPortApi()
}
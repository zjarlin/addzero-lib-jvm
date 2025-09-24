package site.addzero.screens.product.vm

import androidx.lifecycle.ViewModel
import org.koin.android.annotation.KoinViewModel
import site.addzero.generated.isomorphic.DeviceIso
import site.addzero.generated.isomorphic.ProductIso

@KoinViewModel
class DeviceViewModel : ViewModel() {

    fun loadDevices(): List<DeviceIso> {


        return TODO("提供返回值")
    }

    fun loadProducts(): List<ProductIso> {
        return listOf(
            ProductIso(
                id = 1,
                name = "数控机床",
                code = "CNC-001",
                description = "高精度数控机床",
                accessMethod = "MQTT",
                authMethod = "证书认证",
                enabled = true
            ),
            ProductIso(
                id = 2,
                name = "温湿度传感器",
                code = "THS-001",
                description = "温湿度传感器",
                accessMethod = "MQTT",
                authMethod = "密钥认证",
                enabled = true
            )
        )
    }

    fun addDevice(device: DeviceIso) {
        // TODO: 添加设备
    }

    fun updateDevice(device: DeviceIso) {
        // TODO: 更新设备
    }

    fun deleteDevice(id: Long) {
        // TODO: 删除设备
    }
}

